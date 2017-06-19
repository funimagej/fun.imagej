(ns fun.imagej.skeletonize)

(defn skeletonize-2d
  "Skeletonize a 2D binary image."
  [imp]
  (let [plugin (ij.plugin.filter.Binary.)]
    (.setup plugin "skeletonize" imp)
    (.skeletonize plugin)))
        

