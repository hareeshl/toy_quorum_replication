package util;

public class RequestProcessor {

    public static Operation getRequestType(final String request) {
        if(request == null || request.isEmpty()) return null;

        String[] requestContent = request.split(" ");

        //GetItem has 1 arg, PutItem has 2 args. If there are more than that
        //mark as invalid request and move on
        if(requestContent.length >2) return null;

        return Operation.valueOf(requestContent[0]);

    }

}
