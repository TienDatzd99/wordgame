# Download and install Maven
$mavenUrl = "https://archive.apache.org/dist/maven/maven-3/3.9.5/binaries/apache-maven-3.9.5-bin.zip"
$downloadPath = "D:\apache-maven.zip"
$installPath = "D:\maven"

Write-Host "Downloading Maven..."
try {
    Invoke-WebRequest -Uri $mavenUrl -OutFile $downloadPath
    Write-Host "Download completed."
    
    Write-Host "Extracting Maven..."
    Expand-Archive -Path $downloadPath -DestinationPath "D:\" -Force
    
    # Rename to simpler path
    if (Test-Path "D:\apache-maven-3.9.5") {
        if (Test-Path $installPath) {
            Remove-Item $installPath -Recurse -Force
        }
        Rename-Item "D:\apache-maven-3.9.5" $installPath
    }
    
    # Clean up
    Remove-Item $downloadPath -Force
    
    Write-Host "Maven installed successfully at: $installPath"
    Write-Host "To use Maven, run: D:\maven\bin\mvn.cmd"
    
} catch {
    Write-Host "Error installing Maven: $_"
}