; @Context ctxt

;; This is the old Clojure REPL
;(.run (Clojure.Clojure_Interpreter.) "")

(import '[org.scijava Context]
        '[org.scijava.ui.swing.script InterpreterWindow]
        '[org.scijava.script ScriptService])
;        '[org.scijava.`object`.ObjectService

(require '[fun.imagej.core :as ij])

(ij/setup-context (ns-resolve 'user 'ctxt) 'user)

(def context ctxt)
(def interpreter-window (InterpreterWindow. ctxt))

(.show interpreter-window)
(.lang (.getREPL interpreter-window) "Clojure")
;(.eval (.getInterpreter interpreter-window) "(println \"Hello from Clojure\")")
;(.print "Hello!")
