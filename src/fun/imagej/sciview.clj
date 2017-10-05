(ns fun.imagej.sciview
  (:require [fun.imagej.img :as img]
            [fun.imagej.conversion :as convert]
            [fun.imagej.core :as ij]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.img.cursor :as cursor]
            [clojure.string :as string]
            [fun.imagej.ops :as ops]
            [fun.imagej.mesh :as msh]
            [clojure.java.io :as io]))

(defn create-sciview
  "Create a new sciview"
  []
  (let [ctxt (.getContext ij/ij)
        sciview-service (.getService ctxt "sc.iview.SciViewService")
        sciview (.getOrCreateActiveSciView sciview-service)]
    sciview))

;(def sv (create-sciview))

#_(let [ctxt (.getContext ij/ij)
      sciview-service (.getService ctxt "sc.iview.SciViewService")
      sciview (.getOrCreateActiveSciView sciview-service)]
  (.run (.command ij/ij)
        "sc.iview.AddSphere" true (into-array Object ["radius" 25 "sceneryService" sciview-service "sciView" sciview])))

#_(let [ctxt (.getContext ij/ij)
        sciview-service (.getService ctxt "sc.iview.SciViewService")
        sciview (.getOrCreateActiveSciView sciview-service)]
    (.run (.command ij/ij)
          "sc.iview.AddSphere" true (into-array Object ["radius" 25 "sceneryService" sciview-service "sciView" sciview])))

(defn open-obj
  "Open an obj in sciview"
  [filename]
  (let [ctxt (.getContext ij/ij)
        sciview-service (.getService ctxt "sc.iview.SciViewService")
        sciview (.getOrCreateActiveSciView sciview-service)]
    (.run (.command ij/ij)
          "sc.iview.io.ImportObj" true (into-array Object ["objFile" (io/as-file filename) "sciView" sciview]))))

(defn open-stl
  "Open an obj in sciview"
  [filename]
  (let [ctxt (.getContext ij/ij)
        sciview-service (.getService ctxt "sc.iview.SciViewService")
        sciview (.getOrCreateActiveSciView sciview-service)]
    (.run (.command ij/ij)
          "sc.iview.io.ImportSTL" true (into-array Object ["stlFile" (io/as-file filename) "sciView" sciview]))))
