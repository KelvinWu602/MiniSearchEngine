package IRUtilities;

import java.io.IOException;

public class MetadataHandler {
    private HTable<Long, PageMetadata> metadatas;

    public MetadataHandler() throws IOException {
        metadatas = new HTable<Long, PageMetadata>(DBFinder.getHTree(TableNames.ID_METADATA));
    }

    public void addMetadata(Long pageID, PageMetadata metadata) throws IOException {
        metadatas.put(pageID, metadata);
    }

    public boolean pageUpToDate(long pageID, long lastModified) throws IOException {
        //input lastModified : HTTP Header Last Modified
		PageMetadata pagedata = metadatas.get(pageID);
		if(pagedata==null) return false;
		if(pagedata.lastModified < lastModified) return false;
		return true;
    }
}

