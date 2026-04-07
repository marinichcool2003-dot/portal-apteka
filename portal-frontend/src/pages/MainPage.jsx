import { useState } from 'react'
import FirstNav from "../components/FirstNav"
import SecondNav from "../components/SecondNav"
import Content from "../components/mainPage/Content"
import '../styles/index.css'

export default function MainPage() {
    const [isSidebarOpen, setIsSidebarOpen] = useState(true)

    const toggleSidebar = () => {
        setIsSidebarOpen(prev => !prev)
    }

    return(
        <div className={`main ${!isSidebarOpen ? 'sidebar-closed' : ''}`}>
            <FirstNav onToggleSidebar={toggleSidebar} />
            <SecondNav isOpen={isSidebarOpen} onClose={() => setIsSidebarOpen(false)} />
            <Content />
        </div>
    )
}