import './App.css'
import {AuthCallbackHandler} from "./auth/AuthCallbackHandler.tsx";
import { RegisterAccountScreen } from './screens/RegisterAccountScreen.tsx'

function App() {
  return(
  <AuthCallbackHandler>
    <RegisterAccountScreen />
  </AuthCallbackHandler>
  )
}

export default App