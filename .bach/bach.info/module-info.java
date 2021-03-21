import com.github.sormuras.bach.ProjectInfo;
import com.github.sormuras.bach.ProjectInfo.Externals;
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
    })
module bach.info {
  requires com.github.sormuras.bach;
}
