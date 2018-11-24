(ns fun.imagej.n5
  (:require [fun.imagej.img :as img]
            [clojure.string :as string])
  (:import [org.janelia.saalfeldlab.n5.imglib2 N5Utils]
           [org.janelia.saalfeldlab.n5 N5FSReader N5FSWriter GzipCompression]
           (java.util.concurrent Executors)))

(defn open-with-disk-cache
  "Open a N5 dataset with a disk cache"
  [filename data-dir]
  (let [n5 (N5FSReader. filename)]
    (N5Utils/openWithDiskCache n5 data-dir)))

(defn open-n5-cache
  "Create a new cache backed by a N5 dataset.
  Filename is the .n5 and data-directory is where the data actually is"
  [filename]
  (let [writer (N5FSWriter. filename)
        reader (N5FSReader. filename)]
    {:reader reader
     :writer writer}))

(defn args-to-n5-path
  "Construct a n5 path from a fucntion and its args"
  [f args]
  (string/join java.io.File/separator
               (map #(str (hash %))
                    (concat [f] args))))

(defn n5-memoize
  "Returns a memoized version of a referentially transparent function. The
  memoized version of the function keeps a cache of the mapping from arguments
  to results and, when calls with the same arguments are repeated often, has
  higher performance at the expense of higher memory use. [From clojure.core added v1.0]"
  {:static true}
  [f n5-cache]
  (let [exec (Executors/newFixedThreadPool 4)]
    (fn [& args]
      (if-let [e (when (.exists (:reader n5-cache)
                                (args-to-n5-path f args))
                   (N5Utils/open (:reader n5-cache)
                                 (args-to-n5-path f args)))]
        e
        (let [ret (apply f args)]; This is an image that needs to be saved to n5
          (N5Utils/save ret
                        (:writer n5-cache)
                        (args-to-n5-path f args)
                        (int-array (repeat (img/num-dimensions ret) 64))
                        (GzipCompression. 6)
                        exec)
          ret)))))
