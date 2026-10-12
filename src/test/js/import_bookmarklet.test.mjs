import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { test } from "node:test";
import { runInNewContext } from "node:vm";

const source = readFileSync(new URL("../../main/resources/public/import_bookmarklet.js", import.meta.url), "utf8");
const scriptId = "foodinger-import-bookmarklet";

// Minimal DOM harness: exercise the real script and rendered links without
// adding a browser or a dependency to the Java API project.
async function load(query = "", stale = false) {
    const requests = [];
    const elements = new Map();
    const element = () => ({
        innerHTML: "",
        style: {},
        setAttribute() {},
        addEventListener() {},
        querySelector: () => element(),
    });
    const window = { location: { toString: () => "https://recipes.example/carrot" } };
    const document = {
        getElementsByTagName: () => [{ id: scriptId, src: "https://api.example/base/import_bookmarklet.js" + query }],
        getElementById: id => {
            if (!elements.has(id) && id.includes("-content")) elements.set(id, element());
            return elements.get(id);
        },
        createElement: () => element(),
        body: { append: el => elements.set(el.id, el) },
        querySelector: () => null,
        querySelectorAll: () => [],
    };
    const fetch = async (url, request) => {
        requests.push({ url, request });
        if (stale) throw new Error("Expired sign-in");
        const importing = JSON.parse(request.body).query.includes("mutation cookThis");
        return {
            ok: true,
            json: async () => ({ data: importing
                ? { library: { createRecipe: { id: "42" } } }
                : { profile: { me: { imageUrl: null } } } }),
        };
    };
    runInNewContext(source, { window, document, fetch, URL, console: { log() {}, warn() {}, error() {} } });
    const flush = () => new Promise(resolve => setImmediate(resolve));
    await flush();
    const html = () => [...elements.values()].map(el => el.innerHTML).join("\n");
    return { window, requests, html, flush };
}

test("decodes encoded roots and plus signs without failing on malformed percent sequences", async () => {
    const importer = await load("?appRoot=https%3A%2F%2Fapp.example&apiRoot=https%3A%2F%2Fbackend.example&token=synthetic+value%ZZ");
    assert.equal(importer.requests[0].url, "https://backend.example/graphql");
    assert.equal(importer.requests[0].request.headers.Authorization, "Bearer synthetic value%ZZ");
    assert.match(importer.html(), /Cook This!/);
});

test("loads without a query string", async () => {
    const importer = await load();
    assert.equal(importer.requests[0].url, "https://api.example/base/graphql");
    assert.match(importer.html(), /Cook This!/);
});

test("uses the configured app root for stale, detail, and editor links", async () => {
    const query = "?appRoot=http%3A%2F%2Flocalhost%3A4000%2F&apiRoot=http%3A%2F%2Flocalhost%3A8080%2F";
    const expired = await load(query, true);
    assert.match(expired.html(), /href="http:\/\/localhost:4000\/profile#cook-this"/);
    const importer = await load(query);
    const gateway = Object.values(importer.window).find(value => value?.doImport);
    gateway.doImport();
    await importer.flush();
    assert.match(importer.html(), /href="http:\/\/localhost:4000\/recipes\/42"/);
    assert.match(importer.html(), /href="http:\/\/localhost:4000\/recipes\/42\/edit"/);
    assert.match(JSON.parse(importer.requests[1].request.body).query, /cookThis: true/);
});

test("rejects unsafe or malformed roots and escapes HTML attribute characters", async () => {
    for (const root of ["javascript:alert(1)", "data:text/html,test", "not a URL", "https://user:password@app.example"]) {
        const importer = await load("?" + new URLSearchParams({ appRoot: root, apiRoot: root }), true);
        assert.equal(importer.requests[0].url, "https://api.example/base/graphql");
        assert.match(importer.html(), /href="https:\/\/api.example\/base\/profile#cook-this"/);
    }
    const importer = await load("?" + new URLSearchParams({ appRoot: 'https://app.example/path"<tag>&tail' }), true);
    assert.doesNotMatch(importer.html(), /<tag>/);
    assert.match(importer.html(), /%22%3Ctag%3E&amp;tail\/profile#cook-this/);
});
