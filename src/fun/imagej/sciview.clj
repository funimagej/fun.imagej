(ns fun.imagej.sciview
  (:require [fun.imagej.core :as ij])
  (:import (graphics.scenery Node Material HasGeometry BufferUtils)
           (cleargl GLVector)
           (java.nio FloatBuffer)))

(defn get-sciview
  "Return a SciView instance if it exists or create one"
  []
  (let [ctxt (.getContext ij/ij)
        sciview-service (.getService ctxt "sc.iview.SciViewService")
        sciview (.getOrCreateActiveSciView sciview-service)]
    sciview))

;; Core Node types

(defn add-sphere
  "Add a sphere to a sciview instance"
  [sv center radius]
  (let [center (net.imglib2.RealPoint. (double-array center))
        pos (cleargl.GLVector. (float-array [(.getFloatPosition center 0)
                                             (.getFloatPosition center 1)
                                             (.getFloatPosition center 2)]))]
    (.addSphere sv pos radius)))

(defn add-box
  "Add a sphere to a sciview instance"
  [sv position center]
  (.addBox sv position center))

(defn add-obj
  "Add an obj file to a scene (implictly opens the file)"
  [sv filename]
  (.addObj sv filename))

(defn add-stl
  "Add a stl file to a scene (implictly opens the file)"
  [sv filename]
  (.addSTL sv filename))

;; Basics

(defn get-vertices
  "Return the vertices of a HasGeometry"
  [^HasGeometry n]
  (let [vert-buffer (.getVertices n)
        verts (loop [floats []]
                (if (.hasRemaining vert-buffer)
                  (recur (conj floats (.get vert-buffer)))
                  (partition (.getVertexSize n) floats)))]
    (.flip vert-buffer)
    verts))

(defn set-vertices
  "Set the vertices of a geometry"
  [^HasGeometry n verts]
  (let [vert-buffer (.allocateFloatAndPut BufferUtils/BufferUtils (float-array (flatten verts)))]
      (.setVertices n vert-buffer)
      (.recalculateNormals n)
      (.setDirty n true)
      n)
  #_(let [vert-buffer (BufferUtils/allocateFloat (int (count (flatten verts))))]
      (loop [remaining (flatten verts)]
        (when (.hasRemaining vert-buffer)
          (.put vert-buffer (first remaining)))
        (when-not (empty? remaining)
          (recur (rest remaining))))
      (.flip vert-buffer)
      (.setVertices n vert-buffer)
      n))

;; Materials

(defn get-material
  "Return a Scenery Node's material"
  [^Node node]
  (.getMaterial node))

(defn set-material
  "Set a Scenery Node's material"
  [^Node node ^Material mat]
  (.setMaterial node mat))

(defn get-diffuse-color
  "Set the diffuse color of a material."
  [^Material mat]
  (.getDiffuse mat))

(defn set-diffuse-color
  "Set the diffuse color of a material."
  [^Material mat ^GLVector new-color]
  (.setDiffuse mat new-color))

(defn get-specular-color
  "Set the specular color of a material."
  [^Material mat]
  (.getSpecular mat))

(defn set-specular-color
  "Set the specular color of a material."
  [^Material mat ^GLVector new-color]
  (.setSpecular mat new-color))

(defn get-ambient-color
  "Set the ambient color of a material."
  [^Material mat]
  (.getAmbient mat))

(defn set-ambient-color
  "Set the ambient color of a material."
  [^Material mat ^GLVector new-color]
  (.setAmbient mat new-color))

(defn set-color
  "Set all colors (diffuse, specular, ambient) of a Node"
  [^Material mat ^GLVector new-color]
  (set-diffuse-color mat new-color)
  (set-specular-color mat new-color)
  (set-ambient-color mat new-color))

(defn set-double-sided
  "Set whether a material is double-sided"
  [^Material mat new-state]
  (.setDoubleSided mat new-state))

(defn get-double-sided
  "Get whether a material is double-sided"
  [^Material mat]
  (.getDoubleSided mat))

;; Test Snippets

;(def sp (add-sphere (get-sciview) [0 0 0] 10))
;(set-color (get-material sp) (GLVector. (float-array [1 0 0])))

;(def obj (add-obj (get-sciview) "/Users/kharrington/git/brevis/resources/obj/box.obj"))
;(set-color (get-material obj) (GLVector. (float-array [1 0 0])))
;(def verts (get-vertices obj))
;
;(def next-verts
;  (for [v verts]
;    (map (partial * 10) v)))
;
;(set-vertices obj next-verts)

