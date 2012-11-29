package edu.cmu.glimpse.entry;

public class GlimpseEntryPreview {

    private final long mId;
    private final GlimpseEntry mParent;
    private String mPreviewContent;
    private static final int PREVIEW_CONTENT_MAX_LENGTH = 40;

    public GlimpseEntryPreview(long id, GlimpseEntry parent, String entryContent) {
        mId = id;
        mParent = parent;

        String trimmedContent = entryContent.trim();
        int previewContentLength = Math.min(trimmedContent.length(), PREVIEW_CONTENT_MAX_LENGTH);
        mPreviewContent = trimmedContent.substring(0, previewContentLength);

        if (mPreviewContent.length() < trimmedContent.length()) {
            mPreviewContent += "...";
        }
    }

    public long getId() {
        return mId;
    }

    public GlimpseEntry getEntry() {
        return mParent;
    }

    public String getPreviewContent() {
        return mPreviewContent;
    }

    @Override
    public String toString() {
        return mPreviewContent;
    }
}
