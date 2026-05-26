package com.tyfanchz.dynamicscript.enginev1.test;

import com.tyfanchz.dynamicscript.enginev1.annotation.ScriptMethod;
import com.tyfanchz.dynamicscript.enginev1.annotation.ScriptParam;
import com.tyfanchz.dynamicscript.enginev1.annotation.ScriptRunner;

@ScriptRunner
public interface JsAnnotationRunner {
    @ScriptMethod(script = "%1$s * %2$s;")
    Double calMul(double a, double b);

    @ScriptMethod(script = "${a} / ${b}")
    Double calDiv(@ScriptParam("a") double argA, @ScriptParam("b") double argB);

    @ScriptMethod(
        engine = "EMACScript",
        scripts = {
            "var str = '%3$s: Yo${ no }u sho\\\\$uld use \\\\${a} to replace to ${ b  }'",
            "print(str);",
            "print('${b} !== ${a}');",
            "for (var i = 0; i < 100; i++) {",
            "    print('${c} current i = ' + i)",
            "}"
        })
    void formatStr(@ScriptParam("a") String argA, @ScriptParam("b") String argB, @ScriptParam("c") String argC);
}
