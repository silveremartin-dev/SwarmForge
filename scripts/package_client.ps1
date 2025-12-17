# Package SwarmForge Client using jpackage
# Requires Java 21+ JDK with jpackage

$VERSION = "2.0.0"
$APP_NAME = "SwarmForgeClient"
$MAIN_JAR = "swarmforge-client-javafx-2.0.0-SNAPSHOT.jar"
$INPUT_DIR = "target"
$OUTPUT_DIR = "dist"

Write-Host "🚧 Building SwarmForge Client..."
mvn clean package -pl swarmforge-client-javafx -am -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Error "Build failed!"
    exit 1
}

$CLIENT_DIR = "swarmforge-client-javafx"
Push-Location $CLIENT_DIR

# Ensure output directory exists and is empty
if (Test-Path $OUTPUT_DIR) {
    Remove-Item -Recurse -Force $OUTPUT_DIR
}
New-Item -ItemType Directory -Force -Path $OUTPUT_DIR | Out-Null

Write-Host "📦 Creating native package with jpackage..."

# Note: Ideally we would use --module-path and --module, but current project structure is mixed modular/non-modular.
# So we use --input (containing main jar + libs folder) and --main-jar.
# Maven Copy Dependencies puts libs in target/libs.
# jar plugin puts main jar in target.

jpackage `
  --name $APP_NAME `
  --app-version $VERSION `
  --input $INPUT_DIR `
  --main-jar $MAIN_JAR `
  --type app-image `
  --dest $OUTPUT_DIR `
  --win-console `
  --java-options "-Djava.library.path=libs" `
  --description "SwarmForge Eusocial Insect Simulation" `
  --vendor "Silvere Martin-Michiellot"

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Package created successfully in $CLIENT_DIR/$OUTPUT_DIR"
} else {
    Write-Error "jpackage failed!"
}

Pop-Location
