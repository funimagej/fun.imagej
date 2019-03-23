(ns fun.imagej.sciview
  (:require [fun.imagej.core :as ij])
  (:import (graphics.scenery Node Material HasGeometry BufferUtils PointCloud)
           (cleargl GLVector)
           (java.nio FloatBuffer)))

(defn get-sciview
  "Return a SciView instance if it exists or create one"
  []
  (let [ctxt (.getContext (ij/get-ij))
        sciview-service (.getService ctxt "sc.iview.SciViewService")
        sciview (.getOrCreateActiveSciView sciview-service)]
    sciview))

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
  (let [vert-buffer (.allocateFloatAndPut BufferUtils (float-array (flatten verts)))]
      (.setVertices n vert-buffer)
      (.recalculateNormals n)
      (.setDirty n true)
      n))

(defn set-position
  "Set the position of a Node"
  [^Node n new-position]
  (.setPosition n new-position))

(defn get-position
  "Return the position of a Node"
  [^Node n]
  (.getPosition n))

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

;; Core Node types

(let [zero-vec (cleargl.GLVector. (float-array [0 0 0]))]
  (defn move-line
    "Move a line to new start/stop points"
    [line start stop]
    (println "move-line:" (.getCapacity line))
    (.clearPoints line)
    (.addPoint line zero-vec)
    (.addPoint line start)
    (.addPoint line stop)
    (.addPoint line zero-vec)
    line)

  (defn add-line
    "Add a line to a sciview instance"
    ([sv start stop]
      (add-line sv start stop sc.iview.SciView/DEFAULT_COLOR))
    ([sv start stop color]
      (add-line sv start stop color 0.1))
    ([sv start stop color width]
      (let [line (graphics.scenery.Line. 4)]
        (.setEdgeWidth line 0.3)        
        (move-line line start stop)
        (.addNode sv line)))
    #_([sv start stop color width]
      (let [points (into-array sc.iview.vector.Vector3 
                               #_[(sc.iview.vector.ClearGLVector3. start) (sc.iview.vector.ClearGLVector3. stop)]
                               [(sc.iview.vector.ClearGLVector3. zero-vec) (sc.iview.vector.ClearGLVector3. start) (sc.iview.vector.ClearGLVector3. stop) (sc.iview.vector.ClearGLVector3. zero-vec)])]
        (.addLine sv points color width)))))


(defn add-sphere
  "Add a sphere to a sciview instance"
  [sv center radius]
  (let [center (net.imglib2.RealPoint. (double-array center))
        pos (sc.iview.vector.FloatVector3. (.getFloatPosition center 0)
                                           (.getFloatPosition center 1)
                                           (.getFloatPosition center 2))]
    (.addSphere sv pos radius)))

(defn add-box
  "Add a sphere to a sciview instance"
  [sv position size]
  (.addBox sv position size))

(defn add-obj
  "Add an obj file to a scene (implictly opens the file)"
  [sv filename]
  (.addObj sv filename))

(defn add-stl
  "Add a stl file to a scene (implictly opens the file)"
  [sv filename]
  (.addSTL sv filename))

(defn add-volume
  "Add an image to the scene as a volume"
  [sv image]
  (.addVolume sv image))

(defn add-point-cloud
  "Add a point cloud to the scene"
  [sv verts colors point-size]
  (let [flat-verts (flatten verts)
        default-point-size (or point-size 1.025)
        point-cloud (PointCloud. default-point-size "PointCloud")
        mat (Material.)
        v-buffer (.allocateFloat BufferUtils (* (count flat-verts) 4))
        n-buffer (.allocateFloat BufferUtils (* (count flat-verts) 4))
        uv-buffer (.allocateFloat BufferUtils (* (count verts) 2 4))
        uvs (for [k (range (count verts))]
              (repeat 2 default-point-size))
        ]
    (.put v-buffer (float-array flat-verts))
    (.flip v-buffer)
    (.put n-buffer (float-array (flatten colors))); check for RGB v. RGBA
    (.flip n-buffer)
    (.put uv-buffer (float-array (flatten uvs)))
    (.flip uv-buffer)

    (.setVertices point-cloud v-buffer)
    (.setNormals point-cloud n-buffer)
    (.setTexcoords point-cloud uv-buffer)
    (.setIndices point-cloud (.allocateInt BufferUtils 0))
    (.setupPointCloud point-cloud)

    (set-color mat (GLVector. (float-array [1 1 1])))
    (set-material point-cloud mat)
    (set-position point-cloud (GLVector. (float-array [0 0 0])))

    (.addNode sv point-cloud)))

;; Test Snippets

;(def sp (add-sphere (get-sciview) [0 0 0] 10))
;(set-color (get-material sp) (GLVector. (float-array [1 0 0])))

;(def obj (add-obj (get-sciview) "/Users/kharrington/git/brevis/resources/obj/sphere.obj"))
;(set-color (get-material obj) (GLVector. (float-array [1 0 0])))
;
;; First rescale
;(def verts (get-vertices obj))
;(def next-verts
;  (for [v verts]
;    (map (partial * 10) v)))
;(set-vertices obj next-verts)
;
;; Now morph
;(dotimes [k 100]
;  (let [verts (get-vertices obj)
;        next-verts (for [v verts]
;                     (map #(+ % (- (rand) 0.5))
;                          v))]
;    (set-vertices obj next-verts)
;    (Thread/sleep 20)))

#_(add-point-cloud (get-sciview)
                 ; Verts
                 (for [k (range 10)]
                   (for [d (range 3)]
                     (* (rand) 10)))
                 ; Colors
                 (for [k (range 10)]
                   (for [d (range 3)]
                     (rand)))
                 ; UVs
                 1
                 )

;(def sv (get-sciview))
