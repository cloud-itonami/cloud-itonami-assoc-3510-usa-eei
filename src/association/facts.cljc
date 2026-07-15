(ns association.facts
  "Industry rule catalog for the Edison Electric Institute (EEI,
  Wikidata Q5338374) -- a 16th industry-association-level source (see
  cloud-itonami-assoc-6419-jpn-zenginkyo, -6512-jpn-sonpo, -6612-jpn-jsda,
  -6419-deu-bankenverband, -6612-usa-finra, -6512-usa-naic,
  -6920-jpn-jicpa, -6920-usa-aicpa, -6419-fra-fbf, -6511-jpn-seiho,
  -6910-jpn-nichibenren, -6810-jpn-recaj, -6411-jpn-boj, -6120-usa-ctia,
  -5110-usa-a4a for the first fifteen) per ADR-2607141700
  (cloud-itonami-compliance-fact-federation). The FIRST entry aligned to
  ISIC 3510 (electric power generation, transmission and distribution)
  -- a new industry code for this family. A rule not in this table has
  NO spec-basis, full stop; extend `catalog`, do not invent an id/url.

  The Mutual Assistance Agreement PDF was verified by directly reading
  its rendered text via the Read tool (title, member-company obligations,
  and structure confirmed on the document's own first page). The About
  EEI profile page was directly WebFetch-verified; the 1933 founding
  date is corroborated via WebSearch/Wikidata rather than found on the
  About page itself, which states EEI's scope but not its founding
  year.")

(def catalog
  "assoc-slug -> vector of self-regulatory rule entries."
  {"eei"
   [{:association-rule/id "eei.mutual-assistance-agreement"
     :association-rule/title "Edison Electric Institute Mutual Assistance Agreement"
     :association-rule/association "eei"
     :association-rule/isic "3510"
     :association-rule/country "USA"
     :association-rule/kind :self-regulatory-code
     :association-rule/url "https://www.eei.org/-/media/Project/EEI/Documents/Issues-and-Policy/Reliability-and-Emergency-Response/MAAgreementGovPrinc.pdf"
     :association-rule/url-provenance :official-association-site
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:emergency-response :mutual-aid}}
    {:association-rule/id "eei.about-profile"
     :association-rule/title "About EEI (organization profile)"
     :association-rule/association "eei"
     :association-rule/isic "3510"
     :association-rule/country "USA"
     :association-rule/kind :governance-program
     :association-rule/url "https://www.eei.org/en/about-eei/about"
     :association-rule/url-provenance :official-association-site
     :association-rule/established-date "1933"
     :association-rule/retrieved-at "2026-07-15"
     :association-rule/topic #{:governance}}]})

(defn spec-basis [assoc-slug] (get catalog assoc-slug))

(defn coverage
  ([] (coverage (keys catalog)))
  ([slugs]
   (let [have (filter catalog slugs)
         missing (remove catalog slugs)]
     {:requested (count slugs)
      :covered (count have)
      :covered-associations (vec (sort have))
      :missing-associations (vec (sort missing))
      :note (str "cloud-itonami-assoc-3510-usa-eei Wave 0 (ADR-2607141700): "
                 (count (get catalog "eei")) " eei entries seeded with an "
                 "official eei.org citation. Extend "
                 "`association.facts/catalog`, never fabricate a rule id/url.")})))

(defn by-topic [assoc-slug topic]
  (filterv #(contains? (:association-rule/topic %) topic) (spec-basis assoc-slug)))
