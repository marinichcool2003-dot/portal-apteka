import FirstNav from "../components/FirstNav"
import SecondNav from "../components/SecondNav"
import Content from "../components/Content"

import '../styles/index.css'

export default function MainPage() {
    return(
        <div className="main">
            <FirstNav/>
            <SecondNav/>
            <Content/>
        </div>
    )
}
