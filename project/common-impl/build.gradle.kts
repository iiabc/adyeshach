dependencies {
    compileOnly("org.ow2.asm:asm:9.1")
    compileOnly("com.eatthepath:fast-uuid:0.2.0")
    compileOnly(project(":project:common-impl-packetevents"))
    compileOnly("ink.ptms.core:v11904:11904.2-minimize:mapped")
    compileOnly("net.md-5:bungeecord-chat:1.17")
    compileOnly("io.netty:netty-all:4.1.86.Final")
    compileOnly("public:ServerTours:1.6.5")
    compileOnly("public:ServerTours2:2.0.3")
    compileOnly(project(":project:common"))
    // include
    compileOnly("org.spongepowered:math:2.0.1")
}

taboolib { subproject = true }