package com.munzenberger.money.core.version

class MoneyCoreVersionManager : MoneyVersionManager(VersionQueryBuilder("CORE_VERSIONS")) {

    override fun getApplicableVersions() = listOf(
            MoneyCoreVersion_0()
    )
}
