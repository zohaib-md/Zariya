package com.project.zariya.feature.interaction.data.remote

import com.google.gson.annotations.SerializedName

data class OpenFdaResponse(
    @SerializedName("meta") val meta: Meta?,
    @SerializedName("results") val results: List<DrugLabelResult>?
)

data class Meta(
    @SerializedName("disclaimer") val disclaimer: String?,
    @SerializedName("terms") val terms: String?,
    @SerializedName("license") val license: String?,
    @SerializedName("results") val results: MetaResults?
)

data class MetaResults(
    @SerializedName("skip") val skip: Int?,
    @SerializedName("limit") val limit: Int?,
    @SerializedName("total") val total: Int?
)

data class DrugLabelResult(
    @SerializedName("openfda") val openfda: OpenFdaInfo?,
    @SerializedName("drug_interactions") val drug_interactions: List<String>?,
    @SerializedName("warnings") val warnings: List<String>?,
    @SerializedName("adverse_reactions") val adverse_reactions: List<String>?,
    @SerializedName("indications_and_usage") val indications_and_usage: List<String>?,
    @SerializedName("dosage_and_administration") val dosage_and_administration: List<String>?
)

data class OpenFdaInfo(
    @SerializedName("brand_name") val brand_name: List<String>?,
    @SerializedName("generic_name") val generic_name: List<String>?,
    @SerializedName("manufacturer_name") val manufacturer_name: List<String>?,
    @SerializedName("substance_name") val substance_name: List<String>?
)

data class AdverseEventResponse(
    @SerializedName("meta") val meta: Meta?,
    @SerializedName("results") val results: List<AdverseEvent>?
)

data class AdverseEvent(
    @SerializedName("patient") val patient: Patient?
)

data class Patient(
    @SerializedName("drug") val drug: List<DrugInfo>?
)

data class DrugInfo(
    @SerializedName("medicinalproduct") val medicinalproduct: String?,
    @SerializedName("drugcharacterization") val drugcharacterization: String?
)
