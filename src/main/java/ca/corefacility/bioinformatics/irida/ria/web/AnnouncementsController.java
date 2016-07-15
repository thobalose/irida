package ca.corefacility.bioinformatics.irida.ria.web;

import ca.corefacility.bioinformatics.irida.model.announcements.Announcement;
import ca.corefacility.bioinformatics.irida.model.joins.Join;
import ca.corefacility.bioinformatics.irida.model.user.Role;
import ca.corefacility.bioinformatics.irida.model.user.User;
import ca.corefacility.bioinformatics.irida.repositories.specification.AnnouncementSpecification;
import ca.corefacility.bioinformatics.irida.ria.utilities.components.DataTable;
import ca.corefacility.bioinformatics.irida.ria.web.components.datatables.DatatablesUtils;
import ca.corefacility.bioinformatics.irida.service.AnnouncementService;
import ca.corefacility.bioinformatics.irida.service.user.UserService;
import com.github.dandelion.datatables.core.ajax.DataSet;
import com.github.dandelion.datatables.core.ajax.DatatablesCriterias;
import com.github.dandelion.datatables.core.ajax.DatatablesResponse;
import com.github.dandelion.datatables.extras.spring3.ajax.DatatablesParams;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

/**
 *  Controller for handling {@link ca.corefacility.bioinformatics.irida.model.announcements.Announcement} views
 */
@Controller
@RequestMapping(value = "/announcements")
public class AnnouncementsController extends BaseController{

    private static final Logger logger = LoggerFactory.getLogger(AnnouncementsController.class);

    private static final String ANNOUNCEMENT_PAGE = "announcements/announcements";
    private static final String ANNOUNCEMENT_ADMIN_PAGE = "announcements/control";
    private static final String ANNOUNCEMENT_CREATE_PAGE = "announcements/create";
    private static final String ANNOUNCEMENT_DETAIL_PAGE = "announcements/details";

    private static final String SORT_BY_ID = "id";
    private static final String SORT_ASCENDING = "asc";

    final List<String> SORT_COLUMNS = Lists.newArrayList(SORT_BY_ID, "message", "username", "user", "createdDate");

    private final UserService userService;
    private final AnnouncementService announcementService;
    private final MessageSource messageSource;

    @Autowired
    public AnnouncementsController(UserService userService,
                                   AnnouncementService announcementService,
                                   MessageSource messageSource) {
        this.userService = userService;
        this.announcementService = announcementService;
        this.messageSource = messageSource;
    }

    /**
     * Gets a list of {@link Announcement}s for the current {@link User}
     *
     * @param userId
     *              ID of the user for which to get announcements
     * @param model
     *              Model for the view
     * @param principal
     *              The user fetching the announcements (usually the one currently logged in)
     * @return The announcement page containing announcement information for the user
     */
    @RequestMapping(value = "/announcements", method = RequestMethod.GET)
    public String getAllAnnouncementsAsUser(@PathVariable("userId") Long userId,
                                            final Model model, Principal principal) {

        User user = userService.getUserByUsername(principal.getName());
        List<Join<Announcement, User>> joins = announcementService.getReadAnnouncementsForUser(user);
        List<Announcement> announcements = new ArrayList<>();

        for (Join<Announcement, User> j: joins) {
            announcements.add(j.getSubject());
        }

        logger.trace("Announcements list size: " + announcements.size());

        model.addAttribute("announcements", announcements);

        return ANNOUNCEMENT_PAGE;
    }

    /**
     * Get the admin-accessible announcement control page
     *
     * @param model
     *              The model for the returned view
     * @return The announcement control page
     */
    @RequestMapping(value = "/admin")
    public String getControlCentreAdminPage(final Model model) {
        List<Announcement> announcements = announcementService.getAllAnnouncements();

        logger.trace("Announcements list size: " + announcements.size());

        model.addAttribute("announcements", announcements);

        if (model.containsAttribute("errors")) {
            logger.debug("There have been errors! Print this out in a message/dialog");
        }

        return ANNOUNCEMENT_ADMIN_PAGE;
    }

    @RequestMapping(value = "/create", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getCreateAnnouncementPage(final Model model) {

        return ANNOUNCEMENT_CREATE_PAGE;
    }

    /**
     * Create a new announcement
     *
     * @param message
     *              The content of the message for the announcement
     * @param model
     *              The model for the view
     * @param principal
     *              The currently logged in user creating the announcement
     * @return A redirect to the announcement control centre page
     */
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String submitCreateAnnouncement(@RequestParam(required = true) String message,
                                   Model model,
                                   Principal principal) {
        User creator = userService.getUserByUsername(principal.getName());
        Announcement a = new Announcement(message, creator);

        try {
            announcementService.create(a);
        } catch (Exception e) {
            model.addAttribute("errors", "Announcement was not created successfully");
        }

        return getControlCentreAdminPage(model);
    }

    /**
     * Updates an existing announcement object in the database
     *
     * @param announcementID
     *                  The ID of the announcement to be updated
     * @param message
     *                  The content of the updated announcement
     * @param model
     *                  The model for the view
     * @return A redirect to the announcement control center page
     */
    @RequestMapping(value = "/{announcementID}/details", method = RequestMethod.POST)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String submitUpdatedAnnouncement(@PathVariable long announcementID,
                                            @RequestParam(required = false) String message,
                                            Model model) {
        Announcement announcement = announcementService.read(announcementID);
        announcement.setMessage(message);

        try {
            announcementService.update(announcement);
        } catch (Exception e) {
            model.addAttribute("errors", "Announcement was not updated successfully");
            logger.error(e.getMessage());
        }
        return "redirect:/announcements/admin";
    }

    /**
     * Delete an announcement from the database
     *
     * @param model
     *                  The model for the view
     * @param announcementID
     *                  The ID of the announcement to be deleted
     * @return A redirect to the announcement control center page
     */
    @RequestMapping(value = "/{announcementID}/delete")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String deleteAnnouncement(final Model model,
                                     @PathVariable long announcementID) {

        try {
            announcementService.delete(announcementID);
        } catch (Exception e) {
            model.addAttribute("errors", "Announcement could not be deleted");
            logger.error(e.getMessage());
        }

        return "redirect:/announcements/admin";
    }

    /**
     * Get the details page for a particular announcement
     *
     * @param announcementID
     *                  The announcement whose data will be displayed
     * @param model
     *                  The model for the view
     * @param principal
     *                  The currently logged in user (must be an admin)
     * @return Returns the
     * @throws IOException
     */
    @RequestMapping(value = "/{announcementID}/details", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public String getAnnouncementDetailsPage(@PathVariable long announcementID,
                                             Model model,
                                             Principal principal) throws IOException {
        Announcement announcement = announcementService.read(announcementID);
        User user = userService.getUserByUsername(principal.getName());
        if (user.getSystemRole().equals(Role.ROLE_ADMIN)){
            logger.trace("Announcement " + announcement.getId() + ": " +
                announcement.getMessage());

            model.addAttribute("announcement", announcement);

        } else {
            throw new AccessDeniedException("Do not have permissions to edit this announcement");
        }
        return ANNOUNCEMENT_DETAIL_PAGE;
    }

    @RequestMapping(value = "/control/ajax/list", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public @ResponseBody DatatablesResponse<AnnouncementDataTableResponse> getAnnouncementsAdmin(
            final @DatatablesParams DatatablesCriterias criteria, final Principal principal) {
        final int currentPage = DatatablesUtils.getCurrentPage(criteria);
        final Map<String, Object> sortProperties = DatatablesUtils.getSortProperties(criteria);
        final Sort.Direction direction = (Sort.Direction) sortProperties.get("direction");
        String sortName = sortProperties.get("sort_string").toString();
        sortName = sortName.replaceAll("announcement.", "");
        if (sortName.equals("identifier")) {
            sortName = "id";
        }
        if (sortName.equals("createdById")) {
            sortName = "user";
        }

        final String searchString = criteria.getSearch();
        final Page<Announcement> announcements = announcementService.search(AnnouncementSpecification
                .searchAnnouncement(searchString), currentPage, criteria.getLength(), direction, sortName);
        final List<AnnouncementDataTableResponse> announcementDataTableResponseList = announcements.getContent().stream()
                .map(an -> new AnnouncementDataTableResponse(an))
                .collect(Collectors.toList());

        final DataSet<AnnouncementDataTableResponse> announcementDataSet = new DataSet<>(announcementDataTableResponseList,
                announcements.getTotalElements(), announcements.getTotalElements());

        logger.debug("Total number of announcements: " + announcementDataSet.getTotalRecords());
        return DatatablesResponse.build(announcementDataSet, criteria);
    }


    private static final class AnnouncementDataTableResponse {
        private final Announcement announcement;

        public AnnouncementDataTableResponse(final Announcement announcement) {
            this.announcement = announcement;
        }

        public Announcement getAnnouncement() {
            return this.announcement;
        }
    }
}
