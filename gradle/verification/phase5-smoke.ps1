#Requires -Version 7
[CmdletBinding()]
param(
    [Parameter(Mandatory)][string]$Java8,
    [ValidateSet('Client', 'Server', 'PackagedServer')][string]$Mode = 'Server',
    [string[]]$Profile,
    [string]$ForgeServerTemplate,
    [switch]$SurfaceBoundsProbe,
    [string]$WorldFixture,
    [int]$TimeoutSeconds = 150
)

$ErrorActionPreference = 'Stop'
if ($SurfaceBoundsProbe -and $Mode -ne 'Client') { throw 'Surface bounds require a client run.' }
if ($WorldFixture -and $Mode -ne 'PackagedServer' -and !($Mode -eq 'Client' -and $SurfaceBoundsProbe)) {
    throw 'Saved-world checks require packaged Forge or the surface client probe.'
}
$project = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$root = Join-Path $project 'run-phase5'
$manifest = Get-Content -LiteralPath (Join-Path $project 'gradle/ide/phase5-fixtures.json') -Raw | ConvertFrom-Json -AsHashtable
$java = (Resolve-Path -LiteralPath $Java8).Path
$javaRelease = Join-Path (Split-Path (Split-Path $java)) 'release'
if ((Get-Content -LiteralPath $javaRelease -Raw) -notmatch '(?m)^JAVA_VERSION="1\.8\.') {
    throw 'Select the Java 8 executable for the Minecraft runtime.'
}
if (!$Profile) { $Profile = @($manifest.profiles.Keys) }
foreach ($name in $Profile) {
    if (!$manifest.profiles.Contains($name)) { throw "Unknown profile: $name" }
}
$receipt = Join-Path $root ('smoke/' + [DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss-fff') + '-' + $Mode)
New-Item -ItemType Directory -Path $receipt | Out-Null

function Split-LaunchArguments([string]$value) {
    [regex]::Matches($value, '"[^"]*"|[^\s"]+') | ForEach-Object { $_.Value.Trim('"') }
}

foreach ($name in $Profile) {
    $side = if ($Mode -eq 'Client') { 'client' } else { 'server' }
    $run = (New-Item -ItemType Directory -Path (Join-Path $receipt $name)).FullName
    $stage = if ($Mode -eq 'PackagedServer') { Join-Path $root "packaged/$name" } else { Join-Path $root "profiles/$name/$side" }
    if (!(Test-Path -LiteralPath (Join-Path $stage 'mods'))) { throw "Prepare the staged profile first: $stage" }
    Copy-Item -LiteralPath (Join-Path $stage 'mods') -Destination $run -Recurse
    if ($WorldFixture) {
        $worldDestination = Join-Path $run 'smoke-world'
        if ($side -eq 'client') {
            New-Item -ItemType Directory -Path (Join-Path $run 'saves') | Out-Null
            $worldDestination = Join-Path $run 'saves/surface-bounds-world'
        }
        Copy-Item -LiteralPath (Resolve-Path -LiteralPath $WorldFixture).Path -Destination $worldDestination -Recurse
    }
    if ($SurfaceBoundsProbe) {
        Copy-Item -LiteralPath (Join-Path $project 'build/optional-integration-fixture/ironagefurniture-optional-integration-probe-deobf.jar') -Destination (Join-Path $run 'mods')
    }
    if ($side -eq 'server') {
        [IO.File]::WriteAllText((Join-Path $run 'eula.txt'), "eula=true`n")
        [IO.File]::WriteAllLines((Join-Path $run 'server.properties'),
            @('server-ip=127.0.0.1', 'server-port=0', 'online-mode=false', 'level-type=FLAT', 'level-name=smoke-world'))
    }

    $start = [Diagnostics.ProcessStartInfo]::new($java)
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.RedirectStandardInput = $true
    $start.WorkingDirectory = $run
    if ($Mode -eq 'PackagedServer') {
        if (!$ForgeServerTemplate) { throw 'Provide -ForgeServerTemplate pointing to the installed stock Forge server.' }
        $forgeName = "forge-$($manifest.minecraft)-$($manifest.forge)-universal.jar"
        foreach ($entry in @($forgeName, "minecraft_server.$($manifest.minecraft).jar", 'libraries')) {
            Copy-Item -LiteralPath (Join-Path $ForgeServerTemplate $entry) -Destination $run -Recurse
        }
        $probe = Join-Path $project 'build/optional-integration-fixture/ironagefurniture-optional-integration-probe.jar'
        Copy-Item -LiteralPath $probe -Destination (Join-Path $run 'mods')
        $arguments = @('-Xmx2G', "-Diaf.probe.phase5Mods=$($manifest.profiles[$name] -join ',')",
            '-jar', $forgeName, 'nogui')
        if ($WorldFixture) {
            $arguments = @('-Xmx2G', "-Diaf.probe.legacyAuditWorld=$worldDestination",
                '-Diaf.probe.phase5Reload=true', '-jar', $forgeName, 'nogui')
        }
    } else {
        [xml]$launch = Get-Content -LiteralPath (Join-Path $project "Phase5-$name-$side.launch") -Raw
        $attributes = @{}
        foreach ($attribute in $launch.launchConfiguration.stringAttribute) {
            $attributes[$attribute.key] = $attribute.value
        }
        foreach ($map in $launch.launchConfiguration.mapAttribute) {
            if ($map.key -eq 'org.eclipse.debug.core.environmentVariables') {
                foreach ($entry in $map.mapEntry) { $start.Environment[$entry.key] = $entry.value }
            }
        }
        $classpath = foreach ($list in $launch.launchConfiguration.listAttribute) {
            if ($list.key -eq 'org.eclipse.jdt.launching.CLASSPATH') {
                foreach ($entry in $list.listEntry) {
                    $runtimeEntry = ([xml]$entry.value).runtimeClasspathEntry
                    if ($runtimeEntry.containerPath -like 'org.eclipse.jdt.launching.JRE_CONTAINER/*') { continue }
                    $path = $runtimeEntry.externalArchive
                    if (!$path -or !(Test-Path -LiteralPath $path -PathType Leaf)) { throw "Invalid archive in $name launch: $path" }
                    $path
                }
            }
        }
        $arguments = @('-Xmx2G') + @(Split-LaunchArguments $attributes['org.eclipse.jdt.launching.VM_ARGUMENTS']) +
            @('-cp', ($classpath -join [IO.Path]::PathSeparator), $attributes['org.eclipse.jdt.launching.MAIN_TYPE']) +
            @(Split-LaunchArguments $attributes['org.eclipse.jdt.launching.PROGRAM_ARGUMENTS'])
    }
    if ($SurfaceBoundsProbe) {
        $arguments = @('-Diaf.probe.surfaceBoundsClient=true') + $arguments + @('--width', '1280', '--height', '720')
    }
    foreach ($argument in $arguments) { $start.ArgumentList.Add($argument) }
    $process = [Diagnostics.Process]::Start($start)
    $stdout = $process.StandardOutput.ReadToEndAsync()
    $stderr = $process.StandardError.ReadToEndAsync()
    try {
        $deadline = [DateTime]::UtcNow.AddSeconds($TimeoutSeconds)
        $latest = Join-Path $run 'logs/latest.log'
        $ready = $false
        $clientCount = 4 + $manifest.profiles[$name].Count + @($manifest.clientExtras[$name] | Where-Object { $_ }).Count
        while ([DateTime]::UtcNow -lt $deadline) {
            $log = if (Test-Path -LiteralPath $latest) { Get-Content -LiteralPath $latest -Raw } else { '' }
            if ($Mode -eq 'PackagedServer') {
                $fml = Join-Path $run 'logs/fml-server-latest.log'
                $marker = if ($WorldFixture) { 'IAF SAVED SURFACE RELOAD PASSED' } else { 'IAF PHASE 5 STACK PROBE PASSED' }
                $ready = $log -match 'Done \(' -and (Test-Path -LiteralPath $fml) -and
                    (Get-Content -LiteralPath $fml -Raw) -match $marker
            } elseif ($Mode -eq 'Server') {
                $ready = $log -match 'Done \('
            } elseif ($SurfaceBoundsProbe) {
                $ready = Test-Path -LiteralPath (Join-Path $run 'surface-bounds-pass.properties')
            } else {
                $ready = $log -match "Forge Mod Loader has successfully loaded $clientCount mods" -and
                    $log -match 'Created: (?!16x16)\d+x\d+ textures-atlas'
            }
            if ($ready -or $process.HasExited) { break }
            Start-Sleep -Milliseconds 350
        }
        if (!$ready) { throw "$Mode / $name did not reach readiness. See $run." }
        if ($Mode -eq 'Server') { $process.StandardInput.WriteLine('stop') }
        if ($Mode -eq 'Client' -and !$SurfaceBoundsProbe) {
            Start-Sleep -Seconds 2
            if ($process.HasExited) { throw "Client exited after loading: $run" }
            if (!$process.CloseMainWindow() -or !$process.WaitForExit(5000)) {
                if (!$process.HasExited) { $process.Kill($true) }
            }
        }
        if (!$process.WaitForExit(30000)) { throw "$Mode / $name did not exit. See $run." }
        if ($SurfaceBoundsProbe -and $process.ExitCode -ne 0) { throw "Surface bounds client failed: $run" }
        if ($side -eq 'server' -and ($process.ExitCode -ne 0 -or
                (Get-Content -LiteralPath $latest -Raw) -notmatch 'Stopping server')) {
            throw "$Mode / $name did not shut down cleanly. See $run."
        }
        Write-Output "PASS $Mode / $name : $run"
    } finally {
        if (!$process.HasExited) { $process.Kill($true); $process.WaitForExit() }
        [IO.File]::WriteAllText((Join-Path $run 'stdout.log'), $stdout.GetAwaiter().GetResult())
        [IO.File]::WriteAllText((Join-Path $run 'stderr.log'), $stderr.GetAwaiter().GetResult())
        $process.Dispose()
    }
}
