(ns plugins.Scripts.Plugins.fun.imagej.Examples.BBBC003_thresholder)

; @ImagePlus(label="Input image",description="Input image") input-img
; @ImagePlus(label="Target image",description="Target image") target-img
; @OUTPUT ImagePlus thresholded

(require '[fun.imagej.img :as img]
         '[fun.imagej.core :as ij]
         '[fun.imagej.ops :as ops]
         '[fun.imagej.img.shape :as shape]
         '[fun.imagej.conversion :as convert]
         '[fun.imagej.segmentation.imp :as ij1seg]
         '[fun.imagej.imp.roi :as roi]
         '[fun.imagej.img.type :as type])

;(def input-img (ij/open-img "/Users/kharrington/Data/BBBC/mouse_embryos_dic_images/7_19_M1E3.tif"))
;(def target-img (ij/open-img "/Users/kharrington/Data/BBBC/mouse_embryos_dic_foreground/7_19_M1E3.png"))

(def input-imgs (let [input-dir "/Users/kharrington/Data/BBBC/BBBC003_inputs"]
                  (for [file (.listFiles (java.io.File. input-dir))]
                    (ij/open-img (.getPath file)))))

(def target-imgs (let [target-dir "/Users/kharrington/Data/BBBC/BBBC003_targets"]
                   (for [file (.listFiles (java.io.File. target-dir))]
                     (ij/open-img (.getPath file)))))

(defn extract-embryo
  "Extract an embryo parameterized by a radius."
  [input-img radius]
  (convert/imp->img 
    (roi/fill-rois
      (convert/img->imp (img/create-img-like input-img))
      (take-last 1 
                 (sort-by roi/area
                          (ij1seg/imp-to-rois 
                            (convert/img->imp
                              (fun.imagej.ops.threshold/isoData
                                (fun.imagej.ops.filter/variance
                                  (img/create-img-like input-img (type/double-type))
                                  input-img
                                  (shape/sphere-shape radius)))))))
      (repeat 255))))

(let [input-img (first input-imgs)
      target-img (first target-imgs)
      radius 25
      result-img (extract-embryo input-img radius)]
   (ij/show input-img "Input")
   (ij/show target-img "Target")
   (ij/show result-img "Result"))

#_(let [input-img (first input-imgs)
       target-img (first target-imgs)
       filtered (fun.imagej.ops.filter/variance (img/create-img-like input-img (type/double-type))
                                                input-img
                                                (shape/sphere-shape 25))
       thresholded (fun.imagej.ops.threshold/isoData filtered)
       rois (ij1seg/imp-to-rois (convert/img->imp thresholded))
       biggest-roi (convert/imp->img 
                     (roi/fill-rois (convert/img->imp (img/create-img-like thresholded))
                                    (take-last 1 (sort-by roi/area rois))
                                    (repeat 255)))]
   (ij/show filtered "Filtered")
   (ij/show thresholded "Thresholded")
   (ij/show biggest-roi "Biggest roi")
   (ij/show input-img "Input")
   (ij/show target-img "Target"))
