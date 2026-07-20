(ns association-facts-test
  (:require [clojure.java.io :as io] [clojure.java.shell :as shell]
            [clojure.test :refer [deftest is testing]]
            [kotoba.compiler.core :as compiler] [kotoba.compiler.ir :as ir]))
(def source (slurp "src/association_facts.kotoba"))
(defn call [kir function & args] (ir/execute kir function (vec args)))
(defn present [option] (when (second option) (nth option 2)))
(def fields ["id" "title" "association" "isic" "country" "kind" "url"
             "url-provenance" "established-date" "retrieved-at"])
(def expected
  [{"id" "eei.mutual-assistance-agreement"
    "title" "Edison Electric Institute Mutual Assistance Agreement"
    "association" "eei" "isic" "3510" "country" "USA" "kind" "self-regulatory-code"
    "url" "https://www.eei.org/-/media/Project/EEI/Documents/Issues-and-Policy/Reliability-and-Emergency-Response/MAAgreementGovPrinc.pdf"
    "url-provenance" "official-association-site" "established-date" nil
    "retrieved-at" "2026-07-15"}
   {"id" "eei.about-profile" "title" "About EEI (organization profile)"
    "association" "eei" "isic" "3510" "country" "USA" "kind" "governance-program"
    "url" "https://www.eei.org/en/about-eei/about"
    "url-provenance" "official-association-site" "established-date" "1933"
    "retrieved-at" "2026-07-15"}])

(deftest reference-preserves-present-fields-absence-and-topics
  (let [kir (:kir (compiler/compile-source source :js-kotoba-v1))
        observed (mapv (fn [i] (into {} (map (fn [f] [f (present (call kir 'entry-field "eei" i f))]) fields))) [0 1])]
    (is (= expected observed))
    (is (= [nil "1933"] (mapv #(present (call kir 'entry-field "eei" % "established-date")) [0 1])))
    (is (= [2 1] (mapv #(call kir 'topic-count "eei" %) [0 1])))
    (is (= ["emergency-response" "mutual-aid"] (mapv #(present (call kir 'topic "eei" 0 %)) [0 1])))
    (is (= "eei.about-profile" (present (call kir 'by-topic-id "eei" "governance" 0))))
    (is (= #{} (set (:effects kir))))
    (testing "unknown values and invalid indexes fail closed"
      (is (zero? (call kir 'entry-count "aeic")))
      (is (nil? (present (call kir 'entry-field "eei" -1 "id"))))
      (is (nil? (present (call kir 'entry-field "eei" 2 "id"))))
      (is (nil? (present (call kir 'entry-field "eei" 0 "established-date"))))
      (is (nil? (present (call kir 'topic "eei" 1 1))))
      (is (zero? (call kir 'by-topic-count "eei" "labor")))
      (is (nil? (present (call kir 'by-topic-id "eei" "governance" 1)))))))

(defn compiler-root []
  (nth (iterate #(.getParent ^java.nio.file.Path %)
                (java.nio.file.Path/of (.toURI (io/resource "kotoba/compiler/core.clj")))) 4))
(defn base64 [value] (.encodeToString (java.util.Base64/getEncoder) value))
(deftest restricted-javascript-and-typed-wasm-conform-semantically
  (let [javascript (compiler/compile-source source :js-kotoba-v1)
        wasm (compiler/compile-source source :wasm32-browser-kotoba-v1)
        js64 (base64 (.getBytes ^String (:source javascript) "UTF-8")) wasm64 (base64 ^bytes (:bytes wasm))
        probe (shell/sh "node" "--input-type=module" "-e"
                (str "import(process.argv[1]).then(async host=>{const j=await import('data:text/javascript;base64," js64 "');"
                     "const w=await host.instantiateKotoba(Buffer.from(process.argv[2],'base64'));const run=x=>{"
                     "if(x['entry-count']('eei')!==2n||x['entry-field']('eei',0n,'established-date')[1]!==false||x['entry-field']('eei',1n,'established-date')[2]!=='1933')throw Error('dates');"
                     "if(x['topic-count']('eei',0n)!==2n||x['topic']('eei',0n,1n)[2]!=='mutual-aid'||x['topic-count']('eei',1n)!==1n)throw Error('topics');"
                     "if(x['by-topic-id']('eei','governance',0n)[2]!=='eei.about-profile'||x['topic']('eei',1n,1n)[1]!==false)throw Error('query');};"
                     "run(j.instantiateKotoba({}));run(w.instance.exports);}).catch(e=>{console.error(e);process.exit(99)})")
                (.toString (.toUri (.resolve (compiler-root) "runtime/browser-host.mjs"))) wasm64)]
    (is (zero? (:exit probe)) (str (:out probe) (:err probe)))))
(deftest production-source-authority
  (is (= ["src/association_facts.kotoba"]
         (->> (file-seq (io/file "src")) (filter #(.isFile %)) (map str) sort vec))))
