(ns app.core
  (:gen-class)
  (:use markdown.core
        [clojure.string :only (join split)]
        [io.aviso.ansi]
        [io.aviso.logging])
  (:require [clojure.java.io :as io]
            [me.raynes.fs :as fs]
            [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.local :as l]
            [clj-time.format :as f]
            [clojure.string :as str]))


(def default-config-file "_config.json")

(def custom-formatter (f/formatter "yyyy-MM-dd"))

(defn now-str []
  (f/unparse custom-formatter (l/local-now)))

(defn show-banner []
  (println "EXPERIENCE WALL"))

(defn show-help []
  (show-banner)
  (println (join "\n"
                 ["Usage:"
                  "Ewall [command]"])))

(defn read-default-config []
  (parse-string (slurp (io/resource
                        default-config-file)) true))



(def default-config (read-default-config))

(def public-dir (:public_dir default-config))


(defn current-path []
  (str fs/*cwd*))

(defn join-path
  [& args]
  (join "/" args))

(defn join-path-cwd
  [path]
  (println path)
  (join-path (current-path) path))

(defn list-dirs
  [path]
  (filter fs/directory? (fs/list-dir path)))

(defn list-files
  [path]
  (filter fs/file? (fs/list-dir path)))


(defn paths-to-name
  [paths]
  (map fs/name paths))

(defn paths-to-str
  [paths]
  (map str paths))


(defn walk-source-dir
  [root dirs files]
  (println "x")
  (println (str root))
  (println (str dirs))
  (println (first files)))

(defn check-in-wall-project []
  (if-not fs/exists? (join-path (current-path) default-config-file)
          (do
            (println
             (str
              (bold
               (red "Sorry, I can not found \"_config.json\" file!"))))
            (System/exit 0))))

(defn read-project-config
  [path]
  (parse-string (slurp (io/file
                        (join-path path default-config-file))) true))

(defn check-has-source
  [path config]
  (if-not (fs/directory? (join-path path (:source_dir config)))
          (do
            (println (:source_dir config))
            (println
             (str
              (bold
               (red "Sorry, I can not found your source dir !"))))
            (System/exit 0))))

(defn test-md-file [path]
  (if (re-find (re-pattern "\\.md$") path)
    true
    false))

(defn take-file-body-name
  [path]
  (nth (re-find #"([\s\S]+/)([A-Za-z0-9-_\s]+)(.md$)" path) 2))

(take-file-body-name "/home/tyan/DEMO/BNBB/source.md")

(defn list-md-file [path]
  (filter test-md-file (paths-to-str (list-files path))))

(defn check-path-exist-mkdir
  [path]
  (if-not (fs/directory? path)
    (fs/mkdirs path)))


(defn release-dir-file
  [from-path to-path]
  (check-path-exist-mkdir to-path)
  (loop [files (list-md-file from-path)]
    (if (>= 1 (count files))
      (let [md-file (first files)
            file-body-name (take-file-body-name md-file)]
        (println md-file)
        (md-to-html md-file (str to-path "/" file-body-name ".html")))
      (recur (rest files)))))

(defn release-wall [path]
  (check-in-wall-project)
  (let [source-sub-dirs (paths-to-str (list-dirs))
        config (read-project-config)
        wall-path (current-path)]
    (check-has-source wall-path config)
    (let [wall-dirs (list-dirs wall-path)]
      (release-dir-file (join-path wall-path (:source_dir config))
                        (:public_dir config))))
  (fs/walk walk-source-dir path))

;; (defn parse-experience-file
;;   [file-path]
;;   (let [raw-file-content (slurp (io/file file-path))
;;         info-str (first (str/split raw-file-content #"!---"))
;;         info-rows (str/split info-str #"\\n")
;;         info-map (loop [infos info-rows
;;                         result {}]
;;                    (let [info (str/split (first infos) #":")]
;;                      (if (= (count infos) 1)
;;                        (assoc result (first info) (if (or (second info)
;;                                                           (= (str/trim (second info) "")))
;;                                                     (second info)
;;                                                     ""))
;;                        (recur (rest infos) result)))
;;                    )]
;;     (println info-map)))

(defn new-ewall
  [rest]
  (if (< (count rest) 1)
    (do
      (fs/mkdir (join-path-cwd (:source_dir default-config)))
      (fs/mkdir (join-path-cwd (:public_dir default-config)))
      (spit (join-path-cwd default-config-file) (slurp (io/resource
                                                        default-config-file))))
    (do
      (fs/mkdir (join-path-cwd (first rest)))
      (fs/mkdir (join-path-cwd (join-path (first rest) (:source_dir default-config))))
      (fs/mkdir (join-path-cwd (join-path (first rest) (:public_dir default-config))))
      (spit (join-path-cwd (join-path (first rest) default-config-file)) (slurp (io/resource
                                       default-config-file))))))

(defn new-experience
  [rest]
  (if (< (count rest) 1)
    (println
     (str
      (bold
       (red "Sorry, you must input experience name!"))))
    (let [experience-name (first rest)]
      (spit (join-path-cwd (str experience-name ".md"))
            (str
             "--title:" experience-name "\n"
             "--time:" (now-str) "\n"
             "--tag:\n"
             "!---")))))

(defn start-server
  [rest]
  (println))

(defn release-experience
  [rest]
  (println))

(defn -main
  [& args]
  (if (< (count args) 1)
    (show-help)
    (let [command (first args)]
      (cond
        (= command "init") (new-ewall (rest args))
        (= command "new") (new-experience (rest args))
        (= command "release") (release-experience (rest args))
        (= command "serve") (start-server (rest args))
        (= command "help") (show-help)
            :else (show-help)))))
