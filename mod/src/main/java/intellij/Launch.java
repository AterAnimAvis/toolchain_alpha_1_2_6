package intellij;

import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

import com.github.ateranimavis.toploader.modlauncher.launch.MainClientDev;

@SuppressWarnings({"MismatchedReadAndWriteOfArray", "RedundantSuppression", "ConstantConditions", "RedundantOperationOnEmptyContainer"})
public class Launch {

    private static final String[] INJECTED_ARGS = new String[] {

    };

    private static final String[] MIXIN_CONFIGS = new String[] {
        //TODO: Mixin Support either add your mixin configs here or provide them via cmd line
        // "example_mod.mixins.json"
    };

    /**
     * So why a delegating Main? Because Intellij + Gradle integration is dumb it complete ignores the `-cp` option and only loads `:loader` instead of `:mod`
     *
     * @see <a href="https://youtrack.jetbrains.com/issue/IDEA-220528">IDEA-220528</a>
     */
    public static void main(String[] args) throws IOException {
        MainClientDev.main(injectAdditionalArgs(args));
    }

    public static String[] injectAdditionalArgs(String[] args) {
        if (INJECTED_ARGS.length == 0 && MIXIN_CONFIGS.length == 0) return args;

        Stream<String> mixin_config_args = Arrays.stream(MIXIN_CONFIGS).flatMap(it -> Stream.of("--mixin.config", it));
        String[] injected_args = Stream.concat(Arrays.stream(INJECTED_ARGS), mixin_config_args).toArray(String[]::new);

        int thisSize = args.length;
        int arraySize = injected_args.length;
        String[] result = Arrays.copyOf(args, thisSize + arraySize);
        System.arraycopy(injected_args, 0, result, thisSize, arraySize);
        return result;
    }
}
