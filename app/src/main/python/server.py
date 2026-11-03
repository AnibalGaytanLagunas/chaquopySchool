from datetime import datetime, timezone
from os.path import dirname, join
from threading import Lock, Thread
from time import sleep

from flask import Flask, jsonify, render_template
from werkzeug.serving import make_server

HOST = "127.0.0.1"

app = Flask(
    __name__,
    template_folder=join(dirname(__file__), "templates"),
)
_lock = Lock()
_server = None


def current_port():
    return None if _server is None else _server.server_port


@app.get("/")
def index():
    return render_template("index.html", port=current_port())


@app.get("/status")
def status():
    return jsonify(
        status="on",
        host=HOST,
        port=current_port(),
        started_at=datetime.now(timezone.utc).isoformat(),
    )


def start(port=0):
    global _server
    with _lock:
        if _server is not None:
            return _server.server_port

        _server = make_server(HOST, port, app, threaded=True)
        Thread(target=_server.serve_forever, name="flask-http-server", daemon=True).start()
        return _server.server_port


if __name__ == "__main__":
    bound_port = start(5000)
    print(f"Flask en http://{HOST}:{bound_port}/  (Ctrl+C para salir)")
    try:
        while True:
            sleep(1)
    except KeyboardInterrupt:
        if _server is not None:
            _server.shutdown()
