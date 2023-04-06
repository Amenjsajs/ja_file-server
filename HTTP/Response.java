package HTTP;

public class Response {
    public static final int HTTP_OK = 200;
    public static final int HTTP_FORBIDDEN = 403;
    public static final int HTTP_NOT_FOUND = 404;

    public static final int HTTP_ERROR = 500;
    private int status;
    private String content;

    private byte[] bytes;

    public Response(){
        this.status = HTTP_OK;
        this.content = "";
    }

    private Response(int status){
        this.status = status;
    }

    public Response(int status, String content){
        this(status);
        this.content = content;
    }

    public Response(int status, byte[] bytes){
        this(status);
        this.bytes = bytes;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "Response{" +
                "status=" + status +
                ", content='" + content + '\'' +
                '}';
    }
}
