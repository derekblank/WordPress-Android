package org.wordpress.android.ui.mysite.cards.dashboard.plans

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.wordpress.android.analytics.AnalyticsTracker
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.modules.BG_THREAD
import org.wordpress.android.ui.mysite.MySiteCardAndItem.Card.DashboardPlansCard
import org.wordpress.android.ui.mysite.MySiteViewModel.State.SiteSelected
import org.wordpress.android.ui.prefs.AppPrefsWrapper
import org.wordpress.android.util.BuildConfigWrapper
import org.wordpress.android.util.analytics.AnalyticsTrackerWrapper
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Named

class PlansCardUtils @Inject constructor(
    private val appPrefsWrapper: AppPrefsWrapper,
    private val buildConfigWrapper: BuildConfigWrapper,
    private val analyticsTrackerWrapper: AnalyticsTrackerWrapper,
    @Named(BG_THREAD) private val bgDispatcher: CoroutineDispatcher
) {
    private var dashboardUpdateDebounceJob: Job? = null

    private val plansCardVisible = AtomicReference<Boolean?>(null)
    private val waitingToTrack = AtomicBoolean(false)
    private val currentSite = AtomicReference<Int?>(null)

    fun shouldShowCard(
        siteModel: SiteModel,
    ): Boolean {
        return buildConfigWrapper.isJetpackApp &&
                !isCardHiddenByUser(siteModel.siteId) &&
                (siteModel.isWPCom || siteModel.isWPComAtomic) &&
                siteModel.hasFreePlan &&
                siteModel.isAdmin &&
                !siteModel.isWpForTeamsSite
    }

    fun hideCard(siteId: Long) {
        appPrefsWrapper.setShouldHideDashboardPlansCard(siteId, true)
    }

    fun trackCardShown(scope: CoroutineScope, siteSelected: SiteSelected?) {
        // cancel any existing job (debouncing mechanism)
        dashboardUpdateDebounceJob?.cancel()

        dashboardUpdateDebounceJob = scope.launch(bgDispatcher) {
            val isVisible = siteSelected
                ?.dashboardCardsAndItems
                ?.any { card ->
                    card is DashboardPlansCard
                } ?: false

            // add a delay (debouncing mechanism)
            delay(CARD_VISIBLE_DEBOUNCE)

            plansCardVisible.set(isVisible)
            if (isVisible && waitingToTrack.getAndSet(false)) {
                trackCardShown(positionIndex(siteSelected))
            }
        }.also {
            it.invokeOnCompletion { cause ->
                // only set the job to null if it wasn't cancelled since cancellation is part of debouncing
                if (cause == null) dashboardUpdateDebounceJob = null
            }
        }
    }

    fun onResume(siteSelected: SiteSelected?) {
        onDashboardRefreshed(siteSelected)
    }

    fun onSiteChanged(siteId: Int?, siteSelected: SiteSelected?) {
        if (currentSite.getAndSet(siteId) != siteId) {
            plansCardVisible.set(null)
            onDashboardRefreshed(siteSelected)
        }
    }

    fun trackCardTapped(siteSelected: SiteSelected?) {
        analyticsTrackerWrapper.track(
            AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_TAPPED,
            mapOf(POSITION_INDEX to positionIndex(siteSelected))
        )
    }

    fun trackCardMoreMenuTapped(siteSelected: SiteSelected?) {
        analyticsTrackerWrapper.track(
            AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_MORE_MENU_TAPPED,
            mapOf(POSITION_INDEX to positionIndex(siteSelected))
        )
    }

    fun trackCardHiddenByUser(siteSelected: SiteSelected?) {
        analyticsTrackerWrapper.track(
            AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_HIDDEN,
            mapOf(POSITION_INDEX to positionIndex(siteSelected))
        )
    }

    private fun trackCardShown(positionIndex: Int) {
        analyticsTrackerWrapper.track(
            AnalyticsTracker.Stat.DASHBOARD_CARD_PLANS_SHOWN,
            mapOf(POSITION_INDEX to positionIndex)
        )
    }

    private fun isCardHiddenByUser(siteId: Long): Boolean {
        return appPrefsWrapper.getShouldHideDashboardPlansCard(siteId)
    }

    private fun positionIndex(siteSelected: SiteSelected?): Int {
        return siteSelected
            ?.dashboardCardsAndItems
            ?.indexOfFirst {
                it is DashboardPlansCard
            } ?: -1
    }

    private fun onDashboardRefreshed(siteSelected: SiteSelected?) {
        plansCardVisible.get()?.let { isVisible ->
            if (isVisible) trackCardShown(positionIndex(siteSelected))
            waitingToTrack.set(false)
        } ?: run {
            waitingToTrack.set(true)
        }
    }

    companion object {
        const val POSITION_INDEX = "position_index"
        private const val CARD_VISIBLE_DEBOUNCE = 500L
    }
}
