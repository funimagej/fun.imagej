; @File(label="Filename of mesh",description="Filename should be stl") mesh-filename
; @Integer(label="Width") width
; @Integer(label="Height") height
; @Integer(label="Depth") depth
; @Context ctxt
; @OUTPUT ImagePlus voxelized-imp

(ns plugins.Scripts.Plugins.FunImageJ.Mesh.voxelize-mesh
  (:require [fun.imagej.img :as img]
            [fun.imagej.core :as ij]
            [fun.imagej.ops :as ops]
            [fun.imagej.img.shape :as shape]
            [fun.imagej.conversion :as convert]
            [fun.imagej.segmentation.imp :as ij1seg]
            [fun.imagej.img.cursor :as cursor]
            [fun.imagej.imp.roi :as roi]
            [fun.imagej.img.type :as imtype]
            [fun.imagej.mesh :as msh]
            [fun.imagej.imp :as ij1]
            [clojure.string :as string]))

(ij/setup-context (ns-resolve 'user 'ctxt) 'user)

#_(do ; Bindings for testing
  (def mesh-filename (java.io.File. "Cute_Little_Elephant.stl"))
  (def width 100)
  (def height 100)
  (def depth 100))

(let [mesh (msh/read-stl (.getAbsolutePath mesh-filename))
      vox-img (ops/run-op "voxelization" (object-array [mesh width height depth]))
      voxelized-imp (convert/img->imp vox-img)]
  (intern 'user 'voxelized-imp voxelized-imp))
;(ij1/show-imp voxelized-imp)


