package edu.cmu.glimpse.entry;

public class GlimpseEntryPreview {

    private final int mId;
    private String mPreviewContent;
    private static final int PREVIEW_CONTENT_MAX_LENGTH = 40;

    public GlimpseEntryPreview(int id, String entryContent) {
        mId = id;

        String trimmedContent = entryContent.trim();
        int previewContentLength = Math.min(trimmedContent.length(), PREVIEW_CONTENT_MAX_LENGTH);
        mPreviewContent = trimmedContent.substring(0, previewContentLength);

        if (mPreviewContent.length() < trimmedContent.length()) {
            mPreviewContent += "...";
        }
    }

    public String getPreviewContent() {
        return mPreviewContent;
    }

    @Override
    public String toString() {
        return mPreviewContent;
    }
}
