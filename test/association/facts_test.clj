(ns association.facts-test
  (:require [clojure.test :refer [deftest is]]
            [association.facts :as facts]))

(deftest eei-has-spec-basis
  (let [sb (facts/spec-basis "eei")]
    (is (= 2 (count sb)))
    (is (every? #(= "3510" (:association-rule/isic %)) sb))
    (is (every? #(= "USA" (:association-rule/country %)) sb))))

(deftest unknown-association-has-no-spec-basis
  (is (nil? (facts/spec-basis "aeic")))
  (is (nil? (facts/spec-basis "zzz"))))

(deftest coverage-is-honest
  (let [c (facts/coverage ["eei" "aeic"])]
    (is (= 2 (:requested c)))
    (is (= 1 (:covered c)))
    (is (= ["aeic"] (:missing-associations c)))))

(deftest by-topic-filters
  (is (= ["eei.mutual-assistance-agreement"]
         (mapv :association-rule/id (facts/by-topic "eei" :emergency-response))))
  (is (empty? (facts/by-topic "eei" :labor)))
  (is (empty? (facts/by-topic "aeic" :governance))))
