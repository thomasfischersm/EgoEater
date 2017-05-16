package com.playposse.egoeater.backend.sevlets;

import com.google.api.server.spi.response.BadRequestException;
import com.playposse.egoeater.backend.EgoEaterEndPoint;
import com.playposse.egoeater.backend.beans.UserBean;
import com.playposse.egoeater.backend.serveractions.UploadProfilePhotoServerAction;

import org.apache.commons.fileupload.DefaultFileItemFactory;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A servlet to receive profile photos. The Google Endpoint API doesn't handle file uploads.
 * <p>
 * <p>The request takes a sessionId, photoIndex, photo file content.
 * <p>
 * <p>The response is the new photo URL.
 */
public class ProfilePhotoUploadServlet extends HttpServlet {

    private static final Logger log = Logger.getLogger(ProfilePhotoUploadServlet.class.getName());

    private static final String SESSION_ID_FIELD_NAME = "sessionId";
    private static final String PHOTO_INDEX_FIELD_NAME = "photoIndex";
    private static final String PHOTO_CONTENT_FIELD_NAME = "photoContent";

    private static final long FILE_SIZE_MAX = 20_000_000;

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            // Init Apache commons.
            DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
            fileItemFactory.setSizeThreshold((int) FILE_SIZE_MAX);
            ServletFileUpload servletFileUpload = new ServletFileUpload(fileItemFactory);
            servletFileUpload.setFileSizeMax(FILE_SIZE_MAX);
            List<FileItem> fileItems = servletFileUpload.parseRequest(req);

            // Parse the request
            Long sessionId = null;
            Integer photoIndex = null;
            byte[] fileContent = null;
            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {
                    switch (fileItem.getFieldName()) {
                        case PHOTO_CONTENT_FIELD_NAME:
                            fileContent = fileItem.get();
                            break;
                        case SESSION_ID_FIELD_NAME:
                            sessionId = Long.parseLong(fileItem.getString());
                            break;
                        case PHOTO_INDEX_FIELD_NAME:
                            photoIndex = Integer.parseInt(fileItem.getString());
                            break;
                        default:
                            log.info("Got extra form field: " + fileItem.getFieldName());
                            break;
                    }
                }
            }

            // Verify request.
            if ((sessionId == null) || (fileContent == null) || (photoIndex == null)) {
                log.severe("ProfilePhotoUploadServlet received an invalid request. Present: "
                        + (sessionId != null) + " "
                        + (fileContent != null) + " "
                        + (photoIndex != null));
                return;
            }

            // Do the actual work
            UserBean userBean = UploadProfilePhotoServerAction.uploadProfilePhoto(
                    sessionId,
                    photoIndex,
                    fileContent);

            // Generate response.
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            String photoUrl = userBean.getProfilePhotoUrls().get(photoIndex);
            out.print(photoUrl);
        } catch (FileUploadException | BadRequestException ex) {
            log.log(Level.SEVERE, "Failed to parse request for uploaded profile photo.", ex);
        }
    }
}
