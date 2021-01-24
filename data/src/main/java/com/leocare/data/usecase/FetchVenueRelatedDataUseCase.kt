package com.leocare.data.usecase

import com.leocare.data.model.HoursModel
import com.leocare.data.model.VenueModel
import com.leocare.data.model.VenuePhotoModel
import com.leocare.data.network.raw.*
import com.leocare.data.network.ws.VenueDetailWs
import com.leocare.data.network.ws.VenuePhotoWs
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.functions.BiFunction
import java.util.stream.Collectors

/**
 * This use case is in charge of fetch related data of a venue such as photos, contact, address and so one
 */
class FetchVenueRelatedDataUseCase constructor(
        private val mVenueDetailWs: VenueDetailWs,
        private val mVenuePhotoWs: VenuePhotoWs
) {

    companion object {
        // Default image size value
        const val IMAGE_SIZE_URL_PARAMETER = "original"
    }

    /**
     * This method is in charge of fetching venue detail and photos. Then, it builds a [VenueModel]
     * from [VenueDetailWs.fetchVenueDetail] and [VenuePhotoWs.fetchVenuePhotos]
     *
     * @param userToken is the user token for api request
     * @param venue     is the selected venue by the user
     *
     * @return a [Single] containing the [VenueModel]
     * @see convertResponseIntoVenueModel
     */
    fun execute(userToken: String, venue: Venue): Single<VenueModel> {
        val id = venue.id

        return Single.zip(
                mVenueDetailWs.fetchVenueDetail(id, userToken),
                mVenuePhotoWs.fetchVenuePhotos(id, userToken),
                BiFunction<Venue, Photos, Response> { venueDetail, photos ->
                    Response(
                            null,
                            venueDetail,
                            photos
                    )
                }
        )
                .flatMap { convertResponseIntoVenueModel(venue, it) }
    }

    /**
     * This method is in charge of converting json objects [Venue] and [Response] to a [VenueModel]
     * It relies on many local conversion methods with the aim to maintain readability and manage
     * easier nullity
     *
     * @param venue     is the venue json object
     * @param response  is the basic json object of a Foursquare webservice result
     *
     * @return a [Single] that emits the [VenueModel] once all values are retrieved
     */
    private fun convertResponseIntoVenueModel(
            venue: Venue,
            response: Response
    ): Single<VenueModel> {
        return Single.create {
            val id = venue.id
            val name = venue.name
            val description = venue.description ?: ""
            val address = venue.location.address ?: ""
            val city = venue.location.city ?: ""
            val url = venue.url ?: ""
            val like = response.venue?.like ?: false
            val lat = venue.location.lat
            val lng = venue.location.lng
            val phoneNumber = retrievePhoneNumber(response.venue)
            val categoryName = retrieveMainCategoryNameFrom(venue)
            val hoursModel = buildHoursModel(response.venue)
            val venuePhotoList = buildVenuePhotoModelListFrom(response.photos)

            it.onSuccess(
                    VenueModel(
                            id,
                            name,
                            description,
                            address,
                            city,
                            phoneNumber,
                            url,
                            like,
                            lat,
                            lng,
                            categoryName,
                            hoursModel,
                            venuePhotoList
                    )
            )
        }
    }

    /**
     * This method is in charge of building a [HoursModel] from a [venue].
     *
     * @param venue   is the json venue object containing all related venue data
     *
     * @return a [HoursModel] if possible, otherwise null
     *
     * @see buildTimeframeList
     */
    private fun buildHoursModel(venue: Venue?): HoursModel? {
        var hoursModel: HoursModel? = null

        venue?.hours?.apply {
            val isOpen = isOpen
            val lastStatus = status
            val openList = buildTimeframeList(this)

            hoursModel = HoursModel(isOpen, lastStatus, openList)
        }

        return hoursModel
    }

    /**
     * This method is in charge of retrieving and transforming venue related hours into a string list
     * that each item is the hours of a day (or days range).
     *
     * @param hours     is the json object containing open hours of a venue by day or days range and
     *                  the current state (is it open ?)
     *
     * @return a string list containing hours by day or days range concatenated into a string
     */
    private fun buildTimeframeList(hours: Hours): List<String> {
        return hours.timeframes
                .stream()
                .map {
                    var text = it.days
                    if (!it.open.isNullOrEmpty()) {
                        text += "\n" +
                                it.open
                                        .stream()
                                        .map { t -> t.renderedTime }
                                        .collect(Collectors.toList())
                                        .joinToString(", ")
                    }
                    text
                }
                .collect(Collectors.toList())
    }

    /**
     * This method is in charge of retrieving related photo data and creating the [VenuePhotoModel]
     * Url obtained from [Photos.items] is split into two strings, [Item.prefix] and [Item.suffix]
     * When we request the server, we have to give a parameter that will determine the photo size.
     * So the url is built like this : [Item.prefix] + parameter + [Item.suffix]
     * Default size value is [IMAGE_SIZE_URL_PARAMETER].
     *
     * @param photos   is the json object containing photo related data such as url, photographer, etc.
     *
     * @return a list of [VenuePhotoModel] which can be empty
     * @see IMAGE_SIZE_URL_PARAMETER
     */
    private fun buildVenuePhotoModelListFrom(photos: Photos?): ArrayList<VenuePhotoModel> {
        val venuePhotoList = ArrayList<VenuePhotoModel>()

        photos?.items?.apply {
            venuePhotoList.addAll(
                    stream()
                            .filter { item -> item.prefix != null }
                            .filter { item -> item.suffix != null }
                            .map { item -> VenuePhotoModel(item.prefix + IMAGE_SIZE_URL_PARAMETER + item.suffix) }
                            .collect(Collectors.toList())
            )
        }

        return venuePhotoList
    }

    /**
     * This method is in charge of retrieving the venue phone number.
     *
     * @param venue     is the json object containing [Contact], another json object containing
     *                  phone number, facebook and other related contact information of the venue
     *
     * @return phone number of the venue, empty if no one was retrieved
     */
    private fun retrievePhoneNumber(venue: Venue?): String {
        var phoneNumber: String? = null

        venue?.contact?.apply {
            phoneNumber = phone
        }

        return phoneNumber ?: ""
    }

    /**
     * This method is in charge of retrieving the main category name of the venue.
     * This information is contained within [Category] on attribute [Category.primary]
     *
     * @param venue   is the json venue object containing all related venue data
     *
     * @return [Category.name] when one of them is [Category.primary], otherwise empty
     */
    private fun retrieveMainCategoryNameFrom(venue: Venue): String {
        var mainCategoryName: String? = null
        if (venue.categories.isNotEmpty()) {
            venue.categories
                    .stream()
                    .filter { category -> true == category.primary }
                    .findFirst().ifPresent { mainCategoryName = it.name }
        }
        return mainCategoryName ?: ""
    }

}