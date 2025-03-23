window.__cook_this__ = function __cook_this__() {
    function getUrlParts(scripts) {
        const parts = scripts[scripts.length - 1].src.split("?");
        let root = parts[0].split("/");
        root.pop();
        root = root.join("/");
        const querystring = parts[1].split("&")
            .map(p => p.split("="))
            .reduce((m, a) => ({
                ...m,
                [a.shift()]: a.join("="),
            }), {});

        return {
            appRoot: (querystring.appRoot || root),
            apiRoot: (querystring.apiRoot || root) + "/api",
            graphql: (querystring.apiRoot || root) + "/graphql",
            querystring,
        };
    }

    const scripts = [ ...document.getElementsByTagName("script") ]
        .filter(el => el.id === "foodinger-import-bookmarklet");

    const { appRoot, apiRoot, graphql, querystring } = getUrlParts(scripts);
    const headers = querystring.token
        ? { "Authorization": `Bearer ${querystring.token}` }
        : {};

    function gql(query, variables = {}) {
        return fetch(graphql, {
            method: 'POST',
            credentials: "include",
            headers: {
                'Content-Type': 'application/json',
                ...headers,
            },
            body: JSON.stringify({ query, variables }),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Failed to fetch");
                return res.json();
            })
            .then(json => {
                if (json.errors) console.error("FOODINGER ERROR", json.errors[0].message, json.errors);
                if (!json.data) throw new Error("no data");
                return json;
            });
    }

    gql(`query canICookThis { profile { me { imageUrl } } }`)
    .then(json => {
        console.log("COOKTHIS", json)
        store.profileImageUrl = json.data.profile.me.imageUrl;
    })
    .catch(() => store.mode = "stale")
    .finally(() => render());

    const createScratchPhoto = () => {
        if (!store.photoURL) {
            return Promise.resolve(null);
        }
        // use the BFS proxy to avoid CORS problems
        return fetch(apiRoot + "/_image_proxy", {
            credentials: "include",
            headers: {
                ...headers,
                "x-bfs-url": store.photoURL
            },
        })
        .then(response => response.blob())
        .then(blob => {
            return gql(
                    `query cookThisScratchPhoto($type: String! $filename: String!) {
                profile { scratchFile(contentType: $type, originalFilename: $filename) {
                  url
                  cacheControl
                  filename
                }}}`,
                {
                    type: blob.type,
                    filename: store.photoURL.split("/").pop(),
                })
            .then(json => {
                const {
                    url,
                    cacheControl,
                    filename,
                } = json.data.profile.scratchFile;
                return fetch(url, {
                    method: "PUT",
                    headers: {
                        "Cache-Control": cacheControl,
                    },
                    body: blob,
                }).then(() => filename);
            });
        })
        .catch(e => console.error("FOODINGER UPLOAD", e));
    };

    const sendToApi = () => {
        createScratchPhoto().then(scratchPhoto => {
            gql(`mutation cookThis($info: IngredientInfo!) {
              library {
                createRecipe(cookThis: true, info: $info) {
                  id
                }
              }
            }`,
                {
                    info: {
                        type: "Recipe",
                        name: store.title,
                        externalUrl: store.url,
                        ingredients: store.ingredients.map(i => ({ raw: i })),
                        directions: store.directions
                            .map(d => d.trim())
                            .map(d => d.length === 0 ? d : `1.  ${d}`)
                            .join("\n"),
                        calories: store.calories ? Number(store.calories) : null,
                        yield: store.yield ? Number(store.yield) : null,
                        totalTime: store.totalTime ? (Number(store.totalTime) * 60 * 1000) : null,
                        photo: scratchPhoto || null,
                    }
                })
            .then(json => {
                store.mode = "imported";
                store.id = json.data.library.createRecipe.id;
                render();
            })
            .catch(e => {
                alert(`Something went wrong: ${e}\n\nTry it again?`);
            });
            store.mode = "importing";
            render();
        });
    };

    const GATEWAY_PROP = "foodinger_import_bookmarklet_gateway_Ch8LF4wGvsRHN3nM0jFv";
    const CONTAINER_ID = "foodinger-import-bookmarklet-container-Ch8LF4wGvsRHN3nM0jFv";
    const CONTENT_ID = CONTAINER_ID + "-content";
    const store = {
        mode: "form",
        grabTarget: null,
        grabStyle: null,
        title: "",
        url: window.location.toString(),
        ingredients: [],
        directions: [],
        yield: 0,
        totalTime: 0,
        calories: 0,
        photo: null,
        photoURL: "",
        id: null,
        profileImageUrl: null,
    };

    // copied verbatim from the in-app library
    const debounce = (fn, delay = 100) => {
        let timeout = null;
        return (...args) => {
            if (timeout != null) {
                clearTimeout(timeout);
            }
            timeout = setTimeout(() => {
                timeout = null;
                fn(...args);
            }, delay);
        };
    };

    const grabSelectHandler = debounce(() => {
        if (document.getSelection().isCollapsed) return;
        store[store.grabTarget] = (store.grabStyle === "string"
            ? grabString(grabSelectedNode())
            : grabList(grabSelectedNode()));
        tearDownGrab();
    }, 250);

    const findSelectHandler = (e) => {
        e.preventDefault();
        e.stopPropagation();
        if (!e.target.src) return;
        store.photoURL = e.target.src;
        tearDownGrab();
    };

    const listenerOpts = {
        capture: true,
        once: true,
    };

    const setUpGrab = (target, style) => {
        store.mode = "grab";
        store.grabTarget = target;
        store.grabStyle = style;
        document.addEventListener("selectionchange", grabSelectHandler);
        render();
    };

    const setUpFind = (target, style) => {
        store.mode = "find";
        store.grabTarget = target;
        store.grabStyle = style;
        setTimeout(
            () => document.addEventListener("click", findSelectHandler, listenerOpts),
            500,
        );
        render();
    };

    const tearDownGrab = () => {
        store.grabTarget = null;
        store.grabStyle = null;
        store.mode === "find"
            ? document.removeEventListener("click", findSelectHandler, listenerOpts)
            : document.removeEventListener("selectionchange", grabSelectHandler);
        store.mode = "form";
        render();
    };

    const grabSelectedNode =
        () => {
            const sel = document.getSelection();
            if (sel.isCollapsed) {
                alert("Select something to grab...");
                return;
            }
            let targetNode = sel.focusNode;
            while (targetNode && !targetNode.contains(sel.anchorNode)) {
                targetNode = targetNode.parentNode;
            }
            if (!targetNode) {
                alert("Something's amiss. Sorry.");
                return;
            }
            return targetNode;
        };
    const collapseWS = n => n == null ? "" : n.textContent
        .replace(/\s+/g, " ")
        .trim();
    const grabString = node =>
        collapseWS(node);
    const grabList = node =>
        node ? Array.from(node.childNodes)
            .map(collapseWS)
            .filter(s => s.length > 0) : [];
    const findImage = node => {
        if (!node.src) return;
        store.photoURL = node.src;
    };
    const camelToDashed = n =>
        n.replace(/([a-z0-9])([A-Z])/g, (match, a, b) =>
            `${a}-${b.toLowerCase()}`);
    const toStyle = rules =>
        Object.keys(rules)
            .map(n => `${(camelToDashed(n))}:${rules[n]}`)
            .join(";");
    const containerStyle = toStyle({
        position: "fixed",
        top: 0,
        right: 0,
        zIndex: 99999,
        backgroundColor: "whitesmoke",
        border: "1px solid #F57F17",
        borderRightWidth: 0,
        borderTopWidth: 0,
        boxShadow: "0 5px 5px #d3b8ae",
        borderBottomLeftRadius: "5px",
        width: "50%",
        paddingBottom: "1em",
    });
    const headerStyle = toStyle({
        marginTop: 0,
        fontSize: "2rem",
        fontWeight: "bold",
        padding: "0.2em 0.4em",
        backgroundColor: "#F57F17",
        color: "#fff",
    });
    const formItemStyle = toStyle({
        marginTop: "5px",
    });
    const labelStyle = toStyle({
        display: "inline-block",
        textAlign: "right",
        verticalAlign: "top",
        paddingTop: "0.3em",
        marginRight: "0.5em",
        width: "6.5em",
        fontSize: "0.9em",
        fontWeight: "bold",
    });
    const grabBtnStyle = toStyle({
        display: "inline-block",
        verticalAlign: "top",
        backgroundColor: "#ffead9",
        border: "1px solid #ddd",
        padding: "0 0.25em",
        cursor: "pointer",
    });
    const importBtnStyle = toStyle({
        display: "inline-block",
        borderRadius: "0.2em",
        color: "white",
        textTransform: "uppercase",
        backgroundColor: "#F57F17",
        border: "1px solid #ddd",
        fontWeight: "bold",
        padding: "0.5em 1em",
        cursor: "pointer",
    });
    const valueStyle = toStyle({
        width: "75%",
        backgroundColor: "white",
        border: "1px solid #ddd",
    });
    const photoStyle = toStyle({
        width: "85px",
        height: "auto",
        margin: "10px",
    });
    const blockRules = {
        border: "1px solid #ddd",
        backgroundColor: "white",
        width: "75%",
        minWidth: "20em",
        minHeight: "12em",
    };
    const ingStyle = toStyle({
        ...blockRules,
        whiteSpace: "pre",
    });
    const dirStyle = toStyle({
        ...blockRules,
    });
    const drawHeader = title => `<h1 style="${headerStyle}">
        ${title}
        ${store.profileImageUrl ? `<img src="${store.profileImageUrl}" alt="" style="float:right;margin-right:1.7rem;width:2.25rem;height:2.25rem;border-radius:50%" />` : ""}
        </h1>`;
    const renderForm = $div => {
        // noinspection CheckTagEmptyBody
        $div.innerHTML = `${drawHeader("Cook This!")}
        <div style="${formItemStyle}">
            <label style="${labelStyle}">Title:</label>
            <input style="${valueStyle}" name="title" />
            <button style="${grabBtnStyle}" onclick="${GATEWAY_PROP}.grabTitle()">Grab</button>
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}">URL:</label>
            <input style="${valueStyle}" name="url" />
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}">Ingredients:</label>
            <textarea style="${ingStyle}" name="ingredients"></textarea>
            <button style="${grabBtnStyle}" onclick="${GATEWAY_PROP}.grabIngredients()">Grab</button>
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}">Directions:</label>
            <textarea style="${dirStyle}" name="directions"></textarea>
            <button style="${grabBtnStyle}" onclick="${GATEWAY_PROP}.grabDirections()">Grab</button>
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}">Yield:</label>
            <input style="${valueStyle}" name="yield" />
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}">Prep Time:</label>
            <input style="${valueStyle}" name="totalTime" />
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}">Calories:</label>
            <input style="${valueStyle}" name="calories" />
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}"></label>
            <button style="${importBtnStyle}" onclick="${GATEWAY_PROP}.findPhoto()">Find Photo</button>
            ${store.photoURL ? `<img id="photo" src="${store.photoURL}" style="${photoStyle}" alt="photo" />` : ""}
        </div>
        <div style="${formItemStyle}">
            <label style="${labelStyle}"></label>
            <button style="${importBtnStyle}" onclick="${GATEWAY_PROP}.doImport()">Import</button>
        </div>
        `;
        const title = $div.querySelector("input[name=title]");
        title.setAttribute("value", store.title);
        title.addEventListener("change", e => store.title = e.target.value);
        const url = $div.querySelector("input[name=url]");
        url.setAttribute("value", store.url);
        url.addEventListener("change", e => store.url = e.target.value);

        const yieldVal = $div.querySelector("input[name=yield]");
        if (store.yield) {
            yieldVal.setAttribute("value", store.yield);
        }
        yieldVal.addEventListener("change", e => store.yield = e.target.value);

        const calories = $div.querySelector("input[name=calories]");
        if (store.calories) {
            calories.setAttribute("value", store.calories);
        }
        calories.addEventListener(
            "change",
            e => store.calories = e.target.value,
        );

        const totalTime = $div.querySelector("input[name=totalTime]");
        if (store.totalTime) {
            totalTime.setAttribute("value", store.totalTime);
        }
        totalTime.addEventListener(
            "change",
            e => store.totalTime = e.target.value,
        );

        const ings = $div.querySelector("textarea[name=ingredients]");
        ings.innerHTML = store.ingredients.join("\n");
        ings.addEventListener("change", e =>
            store.ingredients = e.target.value
                .split("\n")
                .map(l => l.trim())
                .filter(l => l.length > 0));
        const dirs = $div.querySelector("textarea[name=directions]");
        dirs.innerHTML = store.directions.join("\n");
        dirs.addEventListener("change", e =>
            store.directions = e.target.value
                .split("\n")
                .map(l => l.trim()));
        return {
            grabTitle: () =>
                setUpGrab("title", "string"),
            grabIngredients: () =>
                setUpGrab("ingredients", "list"),
            grabDirections: () =>
                setUpGrab("directions", "list"),
            findPhoto: () =>
                setUpFind("photo", "image"),
            doImport: () =>
                sendToApi(),
        };
    };
    const renderGrab = $div => {
        $div.innerHTML = `${drawHeader(`Grabbing '${store.grabTarget}'`)}
        <p>Select some of the ${store.grabTarget}. Doesn't have to be perfect.
        <button style="${grabBtnStyle}" onclick="${GATEWAY_PROP}.cancel()">Cancel</button>
        </p>`;
        return {
            cancel: () => {
                tearDownGrab();
            },
        };
    };
    const renderFind = $div => {
        $div.innerHTML = `${drawHeader(`Finding '${store.grabTarget}'`)}
        <p>Click on the ${store.grabTarget} you would like to import.
        <button style="${grabBtnStyle}" onclick="${GATEWAY_PROP}.cancel()">Cancel</button>
        </p>`;
        return {
            cancel: () => {
                tearDownGrab();
            },
        };
    };
    const renderStale = $div => {
        $div.innerHTML = `${drawHeader(`Update Cook This!`)}
        <p>Cook This! needs an update. Delete it, reinstall from <a
        href="https://gobrennas.com/profile#cook-this" target="_blank">your
        profile</a>, then return to this page and click it!</p>
        `;
    };
    const renderImporting = $div => {
        $div.innerHTML = `${drawHeader(`Importing...`)}
        <p>Your recipe is being imported. Hang tight...</p>
        `;
    };
    const renderImported = $div => {
        $div.innerHTML = `${drawHeader(`Success!`)}
        <p>Your recipe was successfully imported!</p>
        <p>You can <a href="${appRoot}/library/recipe/${store.id}">view it</a>
        or <a href="${appRoot}/library/recipe/${store.id}/edit">edit it</a>, or
        just continue on your merry way.</p>
        `;
    };
    const render = () => {
        // eslint-disable-next-line no-console
        console.log("FOODINGER", store);
        let $div = document.getElementById(CONTAINER_ID);
        if (!$div) {
            $div = document.createElement("div");
            $div.id = CONTAINER_ID;
            $div.innerHTML = `<div style="all:initial !important">
                <div id="${CONTENT_ID}" style="position:relative;font-family:system-ui,sans-serif""></div>
            </div>
            <a href="#" onclick="${GATEWAY_PROP}.__close(event)" style="position:absolute;top:0.3rem;right:0.5rem;font-weight:bold;font-size:200%;color:#fff;text-decoration:none">×</a>`;
            $div.style = containerStyle;
            document.body.append($div);
        }
        window[GATEWAY_PROP] = {
            ...(store.mode === "form" ? renderForm
                    : store.mode === "grab" ? renderGrab
                        : store.mode === "find" ? renderFind
                            : store.mode === "stale" ? renderStale
                                : store.mode === "importing" ? renderImporting
                                    : store.mode === "imported" ? renderImported
                                        : () => {
                                            throw new Error(`Unrecognized '${store.mode}' mode`);
                                        }
            )(document.getElementById(CONTENT_ID)),
            __close: (e) => {
                e.preventDefault();
                e.stopPropagation();
                $div.parentNode.removeChild($div);
                const $script = document.getElementById(
                    "foodinger-import-bookmarklet");
                $script.parentNode.removeChild($script);
                delete window[GATEWAY_PROP];
            },
        };
    };
    render();

    /** This section autoprocesses based on selectors on common cooking sites **/
    for (const auto of [
        // return truthy to indicate autoprocessing did its thing.
        // () => {
        //     if (!store.url.includes("ssl.barneyb.com")) return;
        //     const r = document.querySelector("html");
        //     store.title = "at " + new Date().toLocaleString();
        //     store.ingredients = grabList(r.querySelector(
        //         "body > ul"));
        //     store.directions = [
        //         ...grabList(r.querySelector("body > h2")),
        //         "and then",
        //         "finally" ];
        //     store.calories = Date.now() % 1000 + 3;
        //     store.yield = Date.now() % 10 + 1;
        //     store.totalTime = Date.now() % 100 + 2;
        //     return true;
        // },
        () => {
            if (!store.url.includes("foodnetwork.com")) return;
            const r = document.querySelector(".o-Recipe");
            if (r == null) return;
            store.title = grabString(r.querySelector(".o-AssetTitle"));
            store.ingredients = grabList(r.querySelector(
                ".o-Ingredients .o-Ingredients__m-Body"));
            store.directions = grabList(r.querySelector(".o-Method ol"));
            return true;
        },
        () => {
            if (!store.url.includes("cooking.nytimes.com")) return;
            const r = document.querySelector("main .recipe");
            store.title = grabString(r.querySelector("[class*=title-display]"));
            store.ingredients = grabList(r.querySelector(
                "[class^=ingredients_ingredients] ul"));
            store.directions = grabList(r.querySelector(
                "[class^=preparation_stepList]"));
            findImage(r.querySelector("[class^=recipeheaderimage] img"));
            store.photo = null;
            return true;
        },
        () => {
            // derived from happymoneysaver.com, hopefully for all WPRM sites?
            const rs = document.querySelectorAll(".wprm-recipe-container");
            if (rs.length !== 1) return;
            const r = rs[0];
            store.title = grabString(r.querySelector(".wprm-recipe-name"));
            store.ingredients = grabList(r.querySelector(
                ".wprm-recipe-ingredients"));
            store.directions = grabList(r.querySelector(
                ".wprm-recipe-instructions"));
            const notes = r.querySelector(".wprm-recipe-notes");
            if (notes != null) store.directions.push(grabString(notes));
            return true;
        },
    ]) {
        try {
            if (auto()) {
                render();
                break;
            }
        } catch (e) {
            // eslint-disable-next-line no-console
            console.warn("auto-import error", e);
        }
    }
}
window.__cook_this__();
