// Public, read-only R2 file server for LongDPC. Supports GET, HEAD and HTTP Range.
const ALLOWED_EXTENSIONS = [".apk", ".apks", ".json"];

function allowedKey(key) {
  return key.length > 0
    && !key.includes("..")
    && ALLOWED_EXTENSIONS.some((extension) => key.toLowerCase().endsWith(extension));
}

export default {
  async fetch(request, env) {
    if (request.method !== "GET" && request.method !== "HEAD") {
      return new Response("Method Not Allowed", {
        status: 405,
        headers: { Allow: "GET, HEAD" },
      });
    }

    const url = new URL(request.url);
    const key = decodeURIComponent(url.pathname.replace(/^\/+/, ""));
    if (!allowedKey(key)) return new Response("Not Found", { status: 404 });

    const options = {
      onlyIf: request.headers,
      range: request.headers,
    };
    const object = await env.FILES.get(key, options);
    if (object === null) return new Response("Not Found", { status: 404 });
    if (!("body" in object)) return new Response(null, { status: 412 });

    const headers = new Headers();
    object.writeHttpMetadata(headers);
    headers.set("etag", object.httpEtag);
    headers.set("accept-ranges", "bytes");
    headers.set("cache-control", "public, max-age=31536000, immutable");

    // R2 honors the incoming Range header. A ranged body must be returned as 206.
    const requestedRange = request.headers.get("range");
    let status = 200;
    if (requestedRange && object.range) {
      const offset = object.range.offset ?? Math.max(0, object.size - (object.range.suffix ?? object.size));
      const length = object.range.length ?? Math.min(object.range.suffix ?? object.size, object.size);
      headers.set("content-range", `bytes ${offset}-${offset + length - 1}/${object.size}`);
      headers.set("content-length", String(length));
      status = 206;
    } else {
      headers.set("content-length", String(object.size));
    }
    return new Response(request.method === "HEAD" ? null : object.body, {
      status,
      headers,
    });
  },
};
