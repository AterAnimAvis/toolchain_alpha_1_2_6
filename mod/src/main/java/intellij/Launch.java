package intellij;

import ateranimavis.launcher.ClientDevLauncher;

public class Launch {

    /**
     * So why a delegating Main? Because Intellij + Gradle integration is dumb it complete ignores the `-cp` option and only loads `:loader` instead of `:mod`
     * @see <a href="https://youtrack.jetbrains.com/issue/IDEA-220528">IDEA-220528</a>
     */
    public static void main(String[] args) {
        ClientDevLauncher.main(args);
    }

}
