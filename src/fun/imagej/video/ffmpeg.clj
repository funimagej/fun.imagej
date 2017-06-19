(ns fun.imagej.video.ffmpeg
  (:use [fun.imagej imp]))        

; This expects that you've made the calls to activate FFMPEG via FIJI
; Super hard-coded for now

(defn save-z-as-avi
  "Save a Z-stack as an avi."
  [imp filename]
  (ij.IJ/run imp "AVI... " (str "compression=JPEG frame=7 save=" filename )))

(defn open-tga-directory
  "Open a directory of tga files as an imagestack."
  [directory]
  (let [listing (.listFiles (java.io.File. directory))]
    (zconcat-imps
      (for [file listing]
        (open-imp (.getAbsolutePath file))))))

(defn tga-sequence-to-avi
  "Take a TGA (RAW) sequence as a directory, and make an avi."
  [directory avi-filename]
  (let [imp (open-tga-directory directory)]
    (save-z-as-avi imp avi-filename)))
