const { app, BrowserWindow } = require('electron')
const path = require('node:path')

const args = process.argv.slice(2);
console.log(args)

app.commandLine.appendSwitch('lang', 'en-US')

function createWindow () {
  // Create the browser window.
  const mainWindow = new BrowserWindow({
    show: false,
    width: 800,
    height: 600,
    autoHideMenuBar: true,
    webPreferences: {
      preload: path.join(__dirname, 'preload.js')
    }
  })

  mainWindow.webContents.on('page-title-updated', (event, title) => {
    console.log(title)
    if (title.includes('Just a moment')) {
      mainWindow.show()
    } else {
      mainWindow.hide()
    }
  })

  mainWindow.webContents.session.on('will-download', (event, item, webContents) => {
    const filePath = path.join(args[0], args[1])
    item.setSavePath(filePath)

    item.on('done', (event, state) => {
      if (state === 'completed') {
        console.log('Download successfully')
      } else {
        console.log(`Download failed: ${state}`)
      }
      app.quit()
    })
  })

  mainWindow.loadURL(args[2])

}

// This method will be called when Electron has finished
// initialization and is ready to create browser windows.
// Some APIs can only be used after this event occurs.
app.whenReady().then(() => {
  createWindow()

  app.on('activate', function () {
    // On macOS it's common to re-create a window in the app when the
    // dock icon is clicked and there are no other windows open.
    if (BrowserWindow.getAllWindows().length === 0) createWindow()
  })
})

// Quit when all windows are closed, except on macOS. There, it's common
// for applications and their menu bar to stay active until the user quits
// explicitly with Cmd + Q.
app.on('window-all-closed', function () {
  if (process.platform !== 'darwin') app.quit()
})

