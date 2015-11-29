(ns app.core
  (:gen-class)
  (:use [clojure.string :only (join split)]
        [io.aviso.ansi]
        [io.aviso.logging]
        markdown.core)
  (:require [cheshire.core :refer :all]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :as fs]))


(def default-config-file "_config.json")


(def custom-formatter (f/formatter "yyyy-MM-dd HH:mm:ss"))

(defn now-str []
  (f/unparse custom-formatter (l/local-now)))

(defn unparse-time
  [time-str]
  (f/parse custom-formatter time-str))



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
  (join-path (current-path) path))

(defn list-dirs
  [path]
  (filter fs/directory? (fs/list-dir path)))

(defn list-dirs-str
  [path]
  (map str (list-dirs path)))


(defn list-dirs-name
  [path]
  (map (fn [path-str]
         (last (split path-str #"/")))
       (list-dirs-str path)))




(defn list-files
  [path]
  (filter fs/file? (fs/list-dir path)))


(defn paths-to-name
  [paths]
  (map fs/name paths))

(defn paths-to-str
  [paths]
  (map str paths))


(defn check-in-wall-project [path]
  (if-not (fs/exists? (join-path path default-config-file))
          (do
            (println
             (str
              (bold
               (red "Sorry, I can not found \"_config.json\" file!"))))
            ;;(System/exit 0)
            )))

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
      ;;(System/exit 0)
      )))

(defn test-md-file [path]
  (if (re-find (re-pattern "\\.md$") path)
    true
    false))

(defn take-file-body-name
  [path]
  (nth (re-find #"([\s\S]+/)([A-Za-z0-9-_\s]+)(.md$)" path) 2))

;;(take-file-body-name "/home/tyan/DEMO/BNBB/source.md")
;;(take-file-body-name "/home/tyan/DEMO/BNBB")

(defn list-md-file [path]
  (filter test-md-file (paths-to-str (list-files path))))

(defn check-path-exist-mkdir
  [path]
  (if-not (fs/directory? path)
    (fs/mkdirs path)))


(defn parse-experience-file
  [file-path]
  
  (defn add-result
    [result info]
    (let [key (subs (first info) 2)
          value (subs (second info) 1)]
      (assoc result
             key
             (if value
               (if (= (str/trim value) "")
                 ""
                 value)
               ""))))
  
  (let [raw-file-content (slurp (io/file file-path))
        info-str (first (str/split raw-file-content #"#---"))
        experience-str (second (str/split raw-file-content #"#---"))
        info-rows (str/split info-str #"\n")
        info-map (loop [infos info-rows
                        result {}]
                   
                   (let [info (let [split-point (.indexOf (first infos) ":")]
                                (map join (split-at split-point (first infos))))]
                     (if (= (count infos) 1)
                       (add-result result info)
                       (recur (rest infos)
                              (add-result result info)))))
        title (get info-map "title")
        date (get info-map "date")
        tag (map str/trim (str/split (get info-map "tag") #","))]
    {:info info-map
     :experience (md-to-html-string experience-str)}))

;;(parse-experience-file "/home/tyan/DEMO/BNBB/source/www.md")

(defn parse-experience-list
  [files-path]
  (map (fn [file-path]
         (parse-experience-file file-path)) files-path))

(defn sort-experience-by-time
  [experiences]
  (sort #(t/after?
          (unparse-time (get (:info %1) "date"))
          (unparse-time (get (:info %2) "date"))) experiences))


(defn package-experience
  [experiences per-page name]
  (loop [experience-packages (partition-all per-page experiences)
         number 0
         result {}]
    (if (= (count experience-packages) 1)
      (assoc result
             (str name "-" number ".json")
             (first experience-packages))
      (recur (rest experience-packages)
             (+ 1 number)
             (assoc result
                    (str name "-" number)
                    (first experience-packages))))))



(defn release-dir-file
  [from-path to-path per-page name]
  (check-path-exist-mkdir to-path)
  (let [experiences (parse-experience-list (list-md-file from-path))]
    (doall (map (fn [experience-package]
                  (spit (join-path to-path (first experience-package))
                        (generate-string (second experience-package)
                                         {:pretty true})))
                (package-experience
                 (map-indexed (fn [idx itm]
                                (assoc itm
                                       :index
                                       idx))
                              (sort-experience-by-time
                               experiences)) per-page name)))
    (count experiences)))


;;(release-wall "/home/tyan/DEMO/BNBB")

(defn release-map-file
  [category-list to-path config]
  (let [mapping {:categoryMap
                 category-list
                 :perPage
                 (:per_page config)}]
    (spit to-path (generate-string mapping {:pretty true}))))

(defn release-wall [path]
  (check-in-wall-project path)
  (let [config (read-project-config path)
        wall-path path]
    (check-has-source wall-path config)
    ;;TODO put all dir
    (let [category-dirs (list-dirs-name (join-path wall-path (:source_dir config)))
          ;;FIXME
          category-list {"uncategorized"
                         (release-dir-file (join-path wall-path (:source_dir config))
                                           (join-path wall-path (:public_dir config) "uncategorized")
                                           (:per_page config)
                                           "uncategorized")}]
      (release-map-file (reduce merge  category-list (map (fn [dir]
                                                            {dir
                                                             (release-dir-file (join-path wall-path (:source_dir config) dir)
                                                                               (join-path wall-path (:public_dir config) dir)
                                                                               (:per_page config)
                                                                               dir)})
                                                          category-dirs))
                        (join-path (join-path wall-path (:public_dir config))
                                   (:mapping_file config))
                        config))))

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
             "--date:" (now-str) "\n"
             "--tag:\n"
             "#---")))))

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
