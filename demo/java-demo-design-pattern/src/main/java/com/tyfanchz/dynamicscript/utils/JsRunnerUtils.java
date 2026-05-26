package com.tyfanchz.dynamicscript.utils;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

public class JsRunnerUtils {
    public static final String DEFAULT_ENGINE_NAME = "nashorn";
    private static final Logger LOGGER = Logger.getLogger(JsRunnerUtils.class.getSimpleName());

    private static ScriptEngine engine;
    private static Map<String, ScriptEngine> engineMap = new HashMap<>();

    static {
        LOGGER.info("---- Initialize JavaScript engines...");

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngineFactory engineFactory = new NashornScriptEngineFactory();

        for (String name : engineFactory.getNames()) {
            engineMap.put(name, engineManager.getEngineByName(name));
        }

        for (String mimeType : engineFactory.getMimeTypes()) {
            engineMap.put(mimeType, engineManager.getEngineByMimeType(mimeType));
        }

        engine = engineMap.get(engineFactory.getNames().get(0));

        LOGGER.info("---- Initialization finished.");
    }

    private JsRunnerUtils() {}

    public static void withEngine(String engineName) {
        engine = engineMap.getOrDefault(engineName, engineMap.get(DEFAULT_ENGINE_NAME));
    }

    public static Object eval(String script) throws Exception {
        Object result;

        result = engine.eval(script);

        return result;
    }
}
