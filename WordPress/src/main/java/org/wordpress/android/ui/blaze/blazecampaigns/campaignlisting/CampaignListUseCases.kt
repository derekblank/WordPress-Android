package org.wordpress.android.ui.blaze.blazecampaigns.campaignlisting

import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.fluxc.store.blaze.BlazeCampaignsStore
import javax.inject.Inject

class FetchCampaignListUseCase @Inject constructor(
    private val store: BlazeCampaignsStore,
    private val mapper: CampaignListingUIModelMapper
) {
    suspend fun execute(site: SiteModel, page: Int): Either<NetworkError, List<CampaignModel>> {
        val result = store.fetchBlazeCampaigns(site, page)
        if (result.isError || result.model == null) return Left(GenericError)
        if (result.model!!.campaigns.isEmpty()) return Left(NoCampaigns)
        val campaigns = result.model!!.campaigns.map { mapper.mapToCampaignModel(it) }
        return Right(campaigns)
    }
}

class GetCampaignListFromDbUseCase @Inject constructor(
    private val store: BlazeCampaignsStore,
    private val mapper: CampaignListingUIModelMapper
) {
    suspend fun execute(site: SiteModel): Either<NoCampaigns, List<CampaignModel>> {
        val result = store.getBlazeCampaigns(site)
        if (result.campaigns.isEmpty()) return Left(NoCampaigns)
        val campaigns = result.campaigns.map { mapper.mapToCampaignModel(it) }
        return Right(campaigns)
    }
}

sealed class Either<out L, out R> {
    val isRight get() = this is Right<R>
    val isLeft get() = this is Left<L>
}

class Left<out L>(val value: L) : Either<L, Nothing>()
class Right<out R>(val value: R) : Either<Nothing, R>()

sealed interface NetworkError

object GenericError : NetworkError

object NoCampaigns : NetworkError
