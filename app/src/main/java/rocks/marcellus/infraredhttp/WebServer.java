package rocks.marcellus.infraredhttp;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    private static final String CommandName = "ir_command";
    private static final String StatusOk = "Beaming \"%s\"";
    private static final String ErrorMethod = "Wrong method";
    private static final String ErrorNotDefined = "Command not specified";

    private ArrayList<ResponseListener> _responseListeners = new ArrayList<>();

    public WebServer(int port) {
        super(port);
    }

    public void AddListener(ResponseListener responseListener)
    {
        if(!_responseListeners.contains(responseListener))
            _responseListeners.add(responseListener);
    }

    public void RemoveListener(ResponseListener responseListener)
    {
        if(_responseListeners.contains(responseListener))
            _responseListeners.remove(responseListener);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Boolean rightMethod = session.getMethod() ==  Method.POST;
        Map<String,String> headers = session.getHeaders();
        Boolean commandSpecified = headers.containsKey(CommandName);

        String irCommand = "";
        if(commandSpecified)
            irCommand = headers.get(CommandName);

        String error;
        if(rightMethod)
        {
            if(commandSpecified)
            {
                for (ResponseListener listener: _responseListeners)
                    listener.OnResponse(irCommand);
                return newFixedLengthResponse(Response.Status.OK, NanoHTTPD.MIME_PLAINTEXT, String.format(StatusOk, irCommand));
            }
            else
                error = ErrorNotDefined;
        }
        else
            error = ErrorMethod;

        return newFixedLengthResponse(Response.Status.BAD_REQUEST, NanoHTTPD.MIME_PLAINTEXT, error);
    }

    public interface ResponseListener
    {
        void OnResponse(String response);
    }
}
