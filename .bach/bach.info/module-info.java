import com.github.sormuras.bach.ProjectInfo;
import com.github.sormuras.bach.ProjectInfo.Externals;
import com.github.sormuras.bach.ProjectInfo.External;
import com.github.sormuras.bach.ProjectInfo.Externals.Name;
import com.github.sormuras.bach.ProjectInfo.Tools;
import com.github.sormuras.bach.ProjectInfo.Tweak;

@ProjectInfo(
    version = "17-bach",
    lookupExternals = @Externals(name = Name.JAVAFX, version = "16"),
    tools = @Tools(limit = {"javac", "jar", "jlink"}),
    tweaks = {
      @Tweak(
          tool = "jlink",
          option = "--launcher",
          value = "worldclock=worldclock/com.carlfx.worldclock.Launcher")
    },
    lookupExternal = {
            @External(module = "com.fasterxml.jackson.core", via = "com.fasterxml.jackson.core:jackson-core:2.12.1"),
            @External(module = "com.fasterxml.jackson.annotation", via = "com.fasterxml.jackson.core:jackson-annotations:2.12.1"),
            @External(module = "com.fasterxml.jackson.databind", via = "com.fasterxml.jackson.core:jackson-databind:2.12.1"),
            @External(module = "com.sothawo", via = "com.fasterxml.jackson.core:jackson-databind:2.12.1")
    })
module bach.info {
  requires com.github.sormuras.bach;
}
