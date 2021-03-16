spigot=$(./gradlew spigot -q)
arguments=$(./gradlew atsArguments -q)
version=$(./gradlew version -q)
anybadge -l version -v "$version" -f version.svg -c blue
anybadge -l spigot -v "$spigot" -f spigot.svg -c blue
anybadge -l atsArguments -v "$arguments" -f arguments.svg -c blue
anybadge -l version -v "$(./gradlew version -q)" -f version.svg -c blue
