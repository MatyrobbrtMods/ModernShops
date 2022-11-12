ModsDotGroovy.make {
    modLoader = 'javafml'
    loaderVersion = '[43,)'

    license = 'MIT'
    issueTrackerUrl = 'https://github.com/MatyrobbrtMods/ModernShops/issues'

    mod {
        modId = 'modernshops'
        displayName = 'Modern Shops'

        version = this.version

        description = 'A mod featuring modern shops for trading'
        authors = ['Matyrobbrt']

        // updateJsonUrl = 'https://maven.moddinginquisition.org/releases/com/matyrobbrt/simpleminers/simpleminers/forge-promotions.json'
        // logoFile = 'modernshops.png'

        dependencies {
            forge = "[${this.forgeVersion},)"
            minecraft = this.minecraftVersionRange

            mod('jei') {
                mandatory = false
                versionRange = "[${this.buildProperties['jei_version']})"
            }
        }
    }
}