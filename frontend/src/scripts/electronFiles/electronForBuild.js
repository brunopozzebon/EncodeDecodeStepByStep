const { app, BrowserWindow } = require("electron");
const isDev = require("electron-is-dev");

const childProcess = require("child_process");
const path = require("path");
const fs = require("fs");

function createWindow() {
  function killProcessIn10022() {
    //TODO descomentar para build
    const opsys = process.platform;
    if (opsys == "darwin") {
      console.log("MAC");
      childProcess.exec("lsof -i tcp:10022", (error, stdout) => {
        const logPortUsed = stdout.split(" ");
        if (logPortUsed.length > 1) {
          childProcess.exec(
            "kill -9 " +
              logPortUsed[logPortUsed.length - 1].substring(
                0,
                logPortUsed.length - 4
              ) +
              " /F"
          );
        }
      });
    } else if (opsys == "win32" || opsys == "win64") {
      console.log("Windows");

      childProcess.exec("netstat -ano | findstr :10022", (error, stdout) => {
        const logPortUsed = stdout.split(" ");
        if (logPortUsed.length > 1) {
          childProcess.exec(
            "taskkill /PID " +
              logPortUsed[logPortUsed.length - 1].substring(
                0,
                logPortUsed.length - 4
              ) +
              " /F"
          );
        }
      });
    } else if (opsys == "linux") {
      console.log("Linux");
      childProcess.exec("lsof -i:10022 -t", (error, stdout) => {
        const logPortUsed = stdout.split(" ");
      });
    }
  }

  killProcessIn10022();
  const jarPath = path.resolve(
    __dirname,
    "..",
    "..",
    "..",
    "public",
    "backend_jar",
    "encodedecodestepbystep-0.0.1-SNAPSHOT.jar"
  );
  console.log(jarPath);

  const child = childProcess.exec("java -jar " + jarPath);

  const win = new BrowserWindow({
    width: 800,
    height: 800,
    webPreferences: {
      nodeIntegration: true,
    },
    icon: __dirname + "/favicon.png",
  });

  win.on("close", (e) => {
    //TODO descomentar para build
    const kill = require("tree-kill");
    kill(child.pid);
    child.kill();
    killProcessIn10022();
  });

  win.loadURL(
    isDev
      ? "http://localhost:3000"
      : `file://${path.join(__dirname, "../build/index.html")}`
  );
}

app.whenReady().then(createWindow);

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});

app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});