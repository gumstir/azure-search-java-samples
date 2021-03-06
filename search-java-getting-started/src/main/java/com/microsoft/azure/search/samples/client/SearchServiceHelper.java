package com.microsoft.azure.search.samples.client;

import com.microsoft.azure.search.samples.demo.AzureSearchConfig;
import com.microsoft.azure.search.samples.options.SearchOptions;
import com.microsoft.azure.search.samples.options.SuggestOptions;

import javax.net.ssl.HttpsURLConnection;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Formatter;
import java.util.function.Consumer;

/**
 * Helper class that contains static helper methods.
 */
public class SearchServiceHelper {
    private static final String _searchURL = "https://%s.search.windows.net/indexes/%s/docs?api-version=%s&search=%s&searchMode=all";
    private static final String _indexUrl = "https://%s.search.windows.net/indexes/%s?api-version=%s";
    private static final String _indexerUrl = "https://%s.search.windows.net/indexers/%s?api-version=%s";
    private static final String _indexingUrl = "https://%s.search.windows.net/indexes/%s/docs/index?api-version=%s";
    private static final String _indexLookupUrl = "https://%s.search.windows.net/indexes/%s/docs('%s')?api-version=%s";

    private static URI buildURI(Consumer<Formatter> fmtFn) {
        Formatter strFormatter = new Formatter();
        fmtFn.accept(strFormatter);
        String url = strFormatter.out().toString();
        strFormatter.close();
        return URI.create(url);
    }

    public static URI getIndexLookupUrl(AzureSearchConfig config, String key) {
        return buildURI(strFormatter -> strFormatter.format(_indexLookupUrl, config.serviceName(), config.indexName(), key, config.apiVersion()));
    }

    public static URI buildSearchUrl(AzureSearchConfig config, String searchTerm, SearchOptions options) {
        var encodedQueryParams = options.toQueryParameters();
        var completeQueryTerm = String.format("%s%s", searchTerm, encodedQueryParams);
        String url = String.format("https://%s.search.windows.net/indexes/%s/docs?api-version=%s&search=%s",
                config.serviceName(), config.indexName(), config.apiVersion(), completeQueryTerm);
        return URI.create(url);
    }

    public static URI buildIndexSuggestUrl(AzureSearchConfig config, String searchTerm, String suggesterName, SuggestOptions options) {
        var encodedQueryParams = options.toQueryParameters();
        String url = String.format(
                "https://%s.search.windows.net/indexes/%s/docs/suggest?api-version=%s&search=%s&suggesterName=%s%s",
                config.serviceName(), config.indexName(), config.apiVersion(), searchTerm, suggesterName, encodedQueryParams);
        return URI.create(url);
    }

    public static URI getIndexUrl(AzureSearchConfig config) {
        return buildURI(strFormatter -> strFormatter.format(_indexUrl, config.serviceName(), config.indexName(), config.apiVersion()));
    }

    public static URI getIndexingUrl(AzureSearchConfig config) {
        return buildURI(strFormatter -> strFormatter.format(_indexingUrl, config.serviceName(), config.indexName(), config.apiVersion()));
    }

    public static void logMessage(String message) {
        System.out.println(message);
    }

    public static boolean isSuccessResponse(HttpResponse<String> response) {
        try {
            int responseCode = response.statusCode();

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_ACCEPTED
                    || responseCode == HttpURLConnection.HTTP_NO_CONTENT || responseCode == HttpsURLConnection.HTTP_CREATED) {
                return true;
            }

            // We got an error
            var msg = response.body();
            if (msg != null) {
                logMessage(String.format("\n ERROR: %s", msg));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static HttpRequest httpRequest(URI uri, String apiKey, String method, String contents) {
        contents = contents == null ? "" : contents;
        var builder = HttpRequest.newBuilder();
        builder.uri(uri);
        builder.setHeader("content-type", "application/json");
        builder.setHeader("api-key", apiKey);

        switch (method) {
            case "GET":
                builder = builder.GET();
                break;
            case "DELETE":
                builder = builder.DELETE();
                break;
            case "PUT":
                builder = builder.PUT(HttpRequest.BodyPublishers.ofString(contents));
                break;
            case "POST":
                builder = builder.POST(HttpRequest.BodyPublishers.ofString(contents));
                break;
            default:
                throw new IllegalArgumentException(String.format("Can't create request for method '%s'", method));
        }
        return builder.build();
    }
}
