package model;

public class FileInfo {
    private String fileName;
    private String fileSize;
    private String timeUpload;

    public FileInfo(String fileName, String fileSize, String timeUpload) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.timeUpload = timeUpload;
    }

    public String getFileName() { return fileName; }
    public String getFileSize() { return fileSize; }
    public String getTimeUpload() { return timeUpload; }
}
