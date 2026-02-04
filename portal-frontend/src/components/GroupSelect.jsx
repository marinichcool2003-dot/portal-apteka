import { useState } from "react";
import { useClientGroups } from "../hooks/GetAllGroups";
import groupAvatar from "../assets/avatars/group-avatars/Аватар группы.png";

export default function GroupSelect() {
  const { groups, loading, error } = useClientGroups();
  const [isOpen, setIsOpen] = useState(false);
  const [selected, setSelected] = useState(null);

  if (loading) return <p>Загрузка...</p>;
  if (error) return <p>Ошибка: {error}</p>;

  return (
    <div className="dropdown">
      <div className="dropdown-header" onClick={() => setIsOpen(!isOpen)}>
        {selected ? selected.name : "Выберите группу"}
      </div>

      {isOpen && (
        <div className="dropdown-list">
          {groups.map((group) => (
            <div
              key={group.id}
              className="dropdown-item"
              onClick={() => {
                setSelected(group);
                setIsOpen(false);
              }}
            >
              <img src={groupAvatar} className="groupAvatar" />
              <div>
                <p>{group.name}</p>
                <small>{group.description}</small>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}