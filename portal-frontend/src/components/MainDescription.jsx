import '../styles/MainDescription.css'

import GroupSelect from './GroupSelect'

export default function MainDescription() {
    return (
        <div className="description-container">
            <div className="description">
                <h1>Управление задачами</h1>
                <h2>Просмотр и управление задачами</h2>
            </div>
            <GroupSelect />
        </div>
    )
}