import { useState } from "react";
import { useClientGroups } from "../hooks/GetAllGroups";
import groupAvatar from "../assets/avatars/group-avatars/Аватар группы.png";
import '../styles/GroupSelect.css'

export default function GroupSelect() {
  const { groups, loading, error } = useClientGroups();
  const [isOpen, setIsOpen] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState(null);

  // Функция для сохранения выбранной группы
  const handleGroupSelect = (group) => {
    setSelectedGroup(group);
    setIsOpen(false);
    
    // Здесь вы можете сохранить ID в переменную или отправить куда нужно
    console.log("Выбрана группа:", {
      id: group.id,
      name: group.name,
      description: group.description
    });
    
    // Можно также передать в родительский компонент через props, если нужно
    // if (onGroupSelect) {
    //   onGroupSelect(group.id);
    // }
  };

  if (loading) return <p className="loading-text">Загрузка групп...</p>;
  if (error) return <p className="error-text">Ошибка: {error}</p>;

  return (
    <div className="group-select-container">
      <div className="group-select">
        <div 
          className="group-select-header"
          onClick={() => setIsOpen(!isOpen)}
        >
          {selectedGroup ? (
            <div className="selected-group">
              <img 
                src={groupAvatar} 
                alt={selectedGroup.name}
                className="selected-group-avatar"
              />
              <div className="selected-group-info">
                <span className="selected-group-name">{selectedGroup.name}</span>
                <span className="selected-group-description">
                  {selectedGroup.description}
                </span>
              </div>
            </div>
          ) : (
            <div className="placeholder">
              <img 
                src={groupAvatar} 
                alt="Выберите группу"
                className="placeholder-avatar"
              />
              <span>Выберите группу</span>
            </div>
          )}
          <span className={`dropdown-arrow ${isOpen ? 'open' : ''}`}>▼</span>
        </div>

        {isOpen && (
          <div className="group-dropdown-list">
            {groups.map((group) => (
              <div
                key={group.id}
                className={`group-dropdown-item ${selectedGroup?.id === group.id ? 'selected' : ''}`}
                onClick={() => handleGroupSelect(group)}
              >
                {/* <img 
                  src={groupAvatar} 
                  alt={group.name}
                  className="group-avatar"
                /> */}
                <div className="group-info">
                  <p className="group-name">{group.name}</p>
                  <small className="group-description">{group.description}</small>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}